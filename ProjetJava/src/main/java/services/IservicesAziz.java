package services;

import java.sql.SQLException;
import java.util.List;

public interface IservicesAziz <T> {
    public void  add(T t) throws SQLException;
    public int  update(T t) throws SQLException;
    public void  delete(T t) throws SQLException;
    public List<T> getAll() throws SQLException;
    public T getone(int id) throws SQLException;
}
