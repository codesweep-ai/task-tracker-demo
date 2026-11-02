package com.acme.tracker.dao;

import com.acme.tracker.db.Database;
import com.acme.tracker.model.Person;
import com.acme.tracker.model.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PersonDao {

    public Person authenticate(String username, String password) {
        // Legacy: plaintext password comparison in SQL.
        String sql = "SELECT id, username, full_name, role FROM person WHERE username = ? AND password = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Person> findAll() {
        List<Person> out = new ArrayList<>();
        String sql = "SELECT id, username, full_name, role FROM person ORDER BY full_name";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    private Person map(ResultSet rs) throws SQLException {
        Person p = new Person();
        p.setId(rs.getLong("id"));
        p.setUsername(rs.getString("username"));
        p.setFullName(rs.getString("full_name"));
        p.setRole(Role.valueOf(rs.getString("role")));
        return p;
    }
}
