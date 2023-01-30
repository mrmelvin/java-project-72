package hexlet.code.domain;

import io.ebean.Model;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import io.ebean.annotation.WhenCreated;

@Entity
public class UrlEntity extends Model {

    @Id
    private int id;

    private String name;

    @WhenCreated
    private Instant createAt;

    public UrlEntity(String urlName) {
        this.name = urlName;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreateAt() {
        return this.createAt;
    }
}
