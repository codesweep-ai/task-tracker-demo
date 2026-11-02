package com.acme.tracker.dao;

import com.acme.tracker.db.Database;
import com.acme.tracker.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {

    private static final String BASE =
        "SELECT t.id, t.title, t.description, t.status, t.priority, t.assignee_id, " +
        "       t.project_id, t.due_date, t.updated_at, a.full_name AS assignee_name, p.name AS project_name " +
        "FROM task t " +
        "JOIN project p ON p.id = t.project_id " +
        "LEFT JOIN person a ON a.id = t.assignee_id";

    public List<Task> findAll() {
        return run(BASE + " ORDER BY t.updated_at DESC");
    }

    public Task findById(long id) {
        List<Task> rows = runWith(BASE + " WHERE t.id = ?", id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Task> findByProject(long projectId) {
        return runWith(BASE + " WHERE t.project_id = ? ORDER BY t.id", projectId);
    }

    /**
     * Finds tasks; the status and assignee filters are optional and the WHERE
     * clause is assembled dynamically. Values are parameterized; only the sort
     * column is concatenated (whitelisted), the classic legacy shortcut.
     */
    public List<Task> search(String status, Long assigneeId, String sort) {
        StringBuilder sql = new StringBuilder(BASE);
        List<Object> params = new ArrayList<>();
        List<String> where = new ArrayList<>();
        if (status != null && !status.isEmpty()) {
            where.add("t.status = ?");
            params.add(status);
        }
        if (assigneeId != null) {
            where.add("t.assignee_id = ?");
            params.add(assigneeId);
        }
        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", where));
        }
        // Whitelist the sort expression to avoid injection via the concatenation.
        sql.append(" ORDER BY ").append(orderClause(sort));

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long create(Task t) {
        String sql = "INSERT INTO task (title, description, status, priority, assignee_id, project_id, due_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getStatus());
            ps.setString(4, t.getPriority());
            setNullableLong(ps, 5, t.getAssigneeId());
            ps.setLong(6, t.getProjectId());
            setNullableDate(ps, 7, t.getDueDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Task t) {
        String sql = "UPDATE task SET title = ?, description = ?, priority = ?, assignee_id = ?, due_date = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getPriority());
            setNullableLong(ps, 4, t.getAssigneeId());
            setNullableDate(ps, 5, t.getDueDate());
            ps.setLong(6, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Raw status write. */
    public void updateStatus(long id, String status) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE task SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Sets status DONE for many ids in a single UPDATE statement. */
    public int bulkClose(long[] ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            in.append(i == 0 ? "?" : ",?");
        }
        String sql = "UPDATE task SET status = 'DONE', updated_at = CURRENT_TIMESTAMP " +
                     "WHERE status <> 'DONE' AND id IN (" + in + ")";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                ps.setLong(i + 1, ids[i]);
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Whitelisted ORDER BY expression. `sort` is a UI token (updated/status/
     * priority/due), never a column name from the request — mapped to a fixed
     * clause here. Default is most-recently-updated first.
     */
    private static String orderClause(String sort) {
        if ("status".equals(sort)) {
            return "t.status, t.updated_at DESC";
        }
        if ("priority".equals(sort)) {
            // Semantic order HIGH > MEDIUM > LOW (not alphabetical).
            return "CASE t.priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END, t.updated_at DESC";
        }
        if ("due".equals(sort) || "due_date".equals(sort)) {
            return "t.due_date, t.updated_at DESC";
        }
        if ("title".equals(sort)) {
            return "t.title, t.id";
        }
        if ("id".equals(sort)) {
            return "t.id";
        }
        return "t.updated_at DESC"; // default, and explicit "updated"
    }

    private static void setNullableLong(PreparedStatement ps, int idx, Long v) throws SQLException {
        if (v == null) {
            ps.setNull(idx, java.sql.Types.BIGINT);
        } else {
            ps.setLong(idx, v);
        }
    }

    private static void setNullableDate(PreparedStatement ps, int idx, String iso) throws SQLException {
        if (iso == null || iso.isEmpty()) {
            ps.setNull(idx, java.sql.Types.DATE);
        } else {
            ps.setDate(idx, java.sql.Date.valueOf(iso));
        }
    }

    private List<Task> run(String sql) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Task> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Task> runWith(String sql, long arg) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, arg);
            try (ResultSet rs = ps.executeQuery()) {
                List<Task> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Task map(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getLong("id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setStatus(rs.getString("status"));
        t.setPriority(rs.getString("priority"));
        long aid = rs.getLong("assignee_id");
        t.setAssigneeId(rs.wasNull() ? null : aid);
        t.setAssigneeName(rs.getString("assignee_name"));
        t.setProjectId(rs.getLong("project_id"));
        t.setProjectName(rs.getString("project_name"));
        java.sql.Date d = rs.getDate("due_date");
        t.setDueDate(d == null ? null : d.toString());
        java.sql.Timestamp u = rs.getTimestamp("updated_at");
        // Trim the fractional seconds: "2026-07-17 14:23:05.123" -> "2026-07-17 14:23:05"
        t.setUpdatedAt(u == null ? null : u.toString().substring(0, 19));
        return t;
    }
}
