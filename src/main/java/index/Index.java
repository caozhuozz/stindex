package index;

public abstract class Index {

    Object root;


    public abstract boolean insert();
    public abstract boolean update();
    public abstract boolean read();
    public abstract boolean delete();
}
