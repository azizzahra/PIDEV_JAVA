package services;
import java.util.List;

public interface Iservices <T> {

    public List<T> getAll() throws Exception;
    void add(T t) throws Exception;
    void update(T t) throws Exception;
    void delete(int id) throws Exception;
    public T getOne(int id) throws Exception;

}