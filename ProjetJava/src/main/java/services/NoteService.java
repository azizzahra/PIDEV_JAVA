package services;

import Main.DatabaseConnection;
import model.Farm;
import model.Note;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteService implements IservicesAziz<Note> {

    private Connection cnx;
    private FarmService farmService;

    public NoteService() {
        cnx = DatabaseConnection.getInstance().getCnx();
        farmService = new FarmService();

        // Verify database connection
        try {
            if (cnx == null || cnx.isClosed()) {
                System.err.println("WARNING: Database connection is null or closed in NoteService constructor");
                cnx = DatabaseConnection.getInstance().getCnx();
            }
        } catch (SQLException e) {
            System.err.println("Error checking database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void add(Note note) throws SQLException {
        // Validate input
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        if (note.getTask() == null || note.getTask().trim().isEmpty()) {
            throw new IllegalArgumentException("Note task cannot be empty");
        }

        if (note.getFarm() == null || note.getFarm().getId() <= 0) {
            throw new IllegalArgumentException("Note must be associated with a valid farm");
        }

        // Check connection
        if (cnx == null || cnx.isClosed()) {
            cnx = DatabaseConnection.getInstance().getCnx();
            System.out.println("Reconnected to database in add method");
        }

        String req = "INSERT INTO integration5.note (task, status, created_at, farm_id) VALUES (?, ?, ?, ?)";
        System.out.println("Executing SQL: " + req + " with params: [" +
                note.getTask() + ", " + note.getStatus() + ", " +
                note.getCreatedAt() + ", " + note.getFarm().getId() + "]");

        PreparedStatement stm = null;
        ResultSet generatedKeys = null;

        try {
            stm = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, note.getTask());
            stm.setString(2, note.getStatus());

            // Make sure createdAt is not null
            if (note.getCreatedAt() == null) {
                note.setCreatedAt(LocalDateTime.now());
            }

            stm.setTimestamp(3, Timestamp.valueOf(note.getCreatedAt()));
            stm.setInt(4, note.getFarm().getId());

            int rowsAffected = stm.executeUpdate();
            System.out.println("Insert executed with " + rowsAffected + " rows affected");

            // Get the generated ID
            generatedKeys = stm.getGeneratedKeys();
            if (generatedKeys.next()) {
                note.setId(generatedKeys.getInt(1));
                System.out.println("New note ID: " + note.getId());
            } else {
                System.err.println("WARNING: Failed to get generated keys for new note");
            }
        } finally {
            // Close resources
            if (generatedKeys != null) {
                try { generatedKeys.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (stm != null) {
                try { stm.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    @Override
    public int update(Note note) throws SQLException {
        // Validate input
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null");
        }

        if (note.getId() <= 0) {
            throw new IllegalArgumentException("Invalid note ID for update");
        }

        // Check connection
        if (cnx == null || cnx.isClosed()) {
            cnx = DatabaseConnection.getInstance().getCnx();
        }

        String req = "UPDATE integration5.note SET task=?, status=?, created_at=?, farm_id=? WHERE id=?";
        System.out.println("Executing update SQL for note ID: " + note.getId());

        PreparedStatement stm = null;
        try {
            stm = cnx.prepareStatement(req);
            stm.setString(1, note.getTask());
            stm.setString(2, note.getStatus());
            stm.setTimestamp(3, Timestamp.valueOf(note.getCreatedAt()));
            stm.setInt(4, note.getFarm().getId());
            stm.setInt(5, note.getId());

            int result = stm.executeUpdate();
            System.out.println("Update executed with " + result + " rows affected");
            return result;
        } finally {
            if (stm != null) {
                try { stm.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    @Override
    public List<Note> getAll() throws SQLException {
        List<Note> notes = new ArrayList<>();

        // Check connection
        if (cnx == null || cnx.isClosed()) {
            cnx = DatabaseConnection.getInstance().getCnx();
        }

        String req = "SELECT * FROM integration5.note";
        System.out.println("Executing getAll query");

        Statement stm = null;
        ResultSet rs = null;

        try {
            stm = cnx.createStatement();
            rs = stm.executeQuery(req);

            while (rs.next()) {
                Note note = extractNoteFromResultSet(rs);
                notes.add(note);
            }

            System.out.println("Retrieved " + notes.size() + " notes");
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (stm != null) {
                try { stm.close(); } catch (SQLException e) { /* ignore */ }
            }
        }

        return notes;
    }

    @Override
    public void delete(Note note) throws SQLException {
        // Validate input
        if (note == null || note.getId() <= 0) {
            throw new IllegalArgumentException("Invalid note for deletion");
        }

        // Check connection
        if (cnx == null || cnx.isClosed()) {
            cnx = DatabaseConnection.getInstance().getCnx();
        }

        String req = "DELETE FROM integration5.note WHERE id=?";
        System.out.println("Executing delete for note ID: " + note.getId());

        PreparedStatement stm = null;
        try {
            stm = cnx.prepareStatement(req);
            stm.setInt(1, note.getId());
            int rowsAffected = stm.executeUpdate();
            System.out.println("Delete executed with " + rowsAffected + " rows affected");
        } finally {
            if (stm != null) {
                try { stm.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    @Override
    public Note getone(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid note ID");
        }

        // Check connection
        if (cnx == null || cnx.isClosed()) {
            cnx = DatabaseConnection.getInstance().getCnx();
        }

        String req = "SELECT * FROM integration5.note WHERE id = ?";
        System.out.println("Getting note with ID: " + id);

        PreparedStatement stm = null;
        ResultSet rs = null;

        try {
            stm = cnx.prepareStatement(req);
            stm.setInt(1, id);
            rs = stm.executeQuery();

            if (rs.next()) {
                Note note = extractNoteFromResultSet(rs);
                System.out.println("Found note: " + note);
                return note;
            } else {
                System.out.println("No note found with ID: " + id);
            }
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (stm != null) {
                try { stm.close(); } catch (SQLException e) { /* ignore */ }
            }
        }

        return null;
    }

    // Method to get all notes from a specific farm
    public List<Note> getNotesByFarm(Farm farm) throws SQLException {
        if (farm == null || farm.getId() <= 0) {
            throw new IllegalArgumentException("Invalid farm for note retrieval");
        }

        return getNotesByFarmId(farm.getId());
    }

    // Added method to get notes by farm ID directly
    public List<Note> getNotesByFarmId(int farmId) throws SQLException {
        if (farmId <= 0) {
            throw new IllegalArgumentException("Invalid farm ID: " + farmId);
        }
        List<Note> notes = new ArrayList<>();
        if (cnx == null || cnx.isClosed()) {
            System.err.println("WARNING: Database connection is null or closed, reconnecting...");
            cnx = DatabaseConnection.getInstance().getCnx();
        }
        String req = "SELECT * FROM integration5.note WHERE farm_id = ?";
        System.out.println("Executing query: " + req + " with farmId=" + farmId);
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = cnx.prepareStatement(req);
            stm.setInt(1, farmId);
            rs = stm.executeQuery();
            while (rs.next()) {
                Note note = extractNoteFromResultSet(rs);
                notes.add(note);
                System.out.println("Found note: ID=" + note.getId() + ", Task=" + note.getTask());
            }
            System.out.println("Total notes retrieved: " + notes.size());
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stm != null) try { stm.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return notes;
    }

    // Method to update the status of a note
    public int updateNoteStatus(int noteId, String status) throws SQLException {
        if (noteId <= 0) {
            throw new IllegalArgumentException("Invalid note ID");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }

        // Check connection
        if (cnx == null || cnx.isClosed()) {
            cnx = DatabaseConnection.getInstance().getCnx();
        }

        String req = "UPDATE integration5.note SET status = ? WHERE id = ?";
        System.out.println("Updating status for note ID " + noteId + " to: " + status);

        PreparedStatement stm = null;
        try {
            stm = cnx.prepareStatement(req);
            stm.setString(1, status);
            stm.setInt(2, noteId);

            int result = stm.executeUpdate();
            System.out.println("Status update executed with " + result + " rows affected");
            return result;
        } finally {
            if (stm != null) {
                try { stm.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    // Helper method to extract a Note from ResultSet
    private Note extractNoteFromResultSet(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("id"));
        note.setTask(rs.getString("task"));
        note.setStatus(rs.getString("status"));

        try {
            // Convert SQL timestamp to LocalDateTime
            Timestamp timestamp = rs.getTimestamp("created_at");
            if (timestamp != null) {
                note.setCreatedAt(timestamp.toLocalDateTime());
            } else {
                note.setCreatedAt(LocalDateTime.now()); // Default to now if null
            }
        } catch (SQLException e) {
            System.err.println("Error getting timestamp: " + e.getMessage());
            note.setCreatedAt(LocalDateTime.now()); // Default to now on error
        }

        try {
            // Get the farm using farm service
            int farmId = rs.getInt("farm_id");
            if (farmId > 0) {
                Farm farm = farmService.getone(farmId);
                note.setFarm(farm);
            } else {
                System.err.println("Warning: Note has invalid farm ID: " + farmId);
                // Create a placeholder farm with just the ID
                Farm placeholderFarm = new Farm();
                placeholderFarm.setId(farmId);
                note.setFarm(placeholderFarm);
            }
        } catch (SQLException e) {
            System.err.println("Error getting farm for note: " + e.getMessage());
        }

        return note;
    }
}