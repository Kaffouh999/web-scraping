package com.ayoub.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DatabseManager {

    private static final String URL = "jdbc:mysql://localhost:3306/JobMarket";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void saveJob(String jobTitle, String companyName, String location, String summary, String postedDate) {
        String sql = "INSERT INTO JobListings (job_title, company_name, location, summary, posted_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, jobTitle);
            pstmt.setString(2, companyName);
            pstmt.setString(3, location);
            pstmt.setString(4, summary);
            pstmt.setDate(5, java.sql.Date.valueOf(postedDate));

            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
