package in.nishantarora.assignment4;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by whizzzkid on 2/11/16.
 */

public class ImageWithFace extends RealmObject {

    String uri, title;
    float x,y,e;
    @PrimaryKey
    String id;

    public String getTitle() {
        return title;
    }
    public String getUri() {
        return uri;
    }
    public String getId() {
        return id;
    }
    public float getx() {
        return x;
    }
    public float gety() {
        return y;
    }
    public float gete() {
        return e;
    }
    public void setx(float x) {
        this.x = x;
    }
    public void sety(float y) {
        this.y = y;
    }
    public void sete(float e) {
        this.e = e;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
}
