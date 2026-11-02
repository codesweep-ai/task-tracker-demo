package com.acme.tracker.dao;

import com.acme.tracker.db.Database;
import com.acme.tracker.model.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectDao {

    private static final String BASE =
        "SELECT p.id, p.name, p.description, p.state, p.owner_id, o.full_name AS owner_name " +
        "FROM project p JOIN person o ON o.id = p.owner_id";

    public List<Project> findAll() {
        List<Project> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + " ORDER BY p.name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public Project findById(long id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + " WHERE p.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean exists(long id) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM project WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long create(Project p) {
        String sql = "INSERT INTO project (name, description, state, owner_id) VALUES (?, ?, 'ACTIVE', ?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setLong(3, p.getOwnerId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Moves a project from ACTIVE to ARCHIVED. */
    public void archive(long id) {
        String sql = "UPDATE project SET state = 'ARCHIVED' WHERE id = ? AND state = 'ACTIVE'";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Project map(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setState(rs.getString("state"));
        p.setOwnerId(rs.getLong("owner_id"));
        p.setOwnerName(rs.getString("owner_name"));
        return p;
    }
}
