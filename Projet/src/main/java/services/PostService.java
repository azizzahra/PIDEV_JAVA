package services;

import Main.DatabaseConnection;
import model.post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostService implements Iservices<post>
{

    Connection cnx;

    public PostService() {
        cnx = DatabaseConnection.getInstance().getCnx();
    }

    @Override
    public void add(post post) {
        String req = "INSERT INTO integration5.post (title, username_id, content, category, attachment, date_c, views, vote) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stm = cnx.prepareStatement(req);
            stm.setString(1, post.getTitle());
            stm.setInt(2, post.getUsernameId());
            stm.setString(3, post.getContent());
            stm.setString(4, post.getCategory());
            stm.setString(5, post.getAttachment());
            stm.setString(6, post.getDateC());
            stm.setInt(7, post.getViews());
            stm.setInt(8, post.getVote());
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modify(post post) {
        // Implementation for modifying a post (if needed)
    }

    @Override
    public List<post> afficher() {
        List<post> posts = new ArrayList<>();
        String req = "SELECT * FROM post";

        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(req);

            while (rs.next()) {
                post post = new post();
                post.setId(rs.getInt("id"));
                post.setTitle(rs.getString("title"));
                post.setUsernameId(rs.getInt("username_id"));
                post.setContent(rs.getString("content"));
                post.setCategory(rs.getString("category"));
                post.setAttachment(rs.getString("attachment"));
                post.setDateC(rs.getString("date_c"));
                post.setViews(rs.getInt("views"));
                post.setVote(rs.getInt("vote"));

                posts.add(post);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println(posts);
        return posts;
    }

    @Override
    public post getone() {
        return null;
    }
}
