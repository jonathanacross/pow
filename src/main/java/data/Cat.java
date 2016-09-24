package data;

public class Cat extends Animal {

    private String favoriteToy;

    public Cat() { }

    public Cat(String name, String favoriteToy) {
        setName(name);
        setFavoriteToy(favoriteToy);
    }

    public String getFavoriteToy() { return favoriteToy; }
    public void setFavoriteToy(String favoriteToy) { this.favoriteToy = favoriteToy; }
}
