package hexlet.code.domain;

import java.util.List;
import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;

@Entity
public class Url extends Model {

    @Id
    private int id;

    private String name;

    @WhenCreated
    private Instant createAt;

    @OneToMany(cascade = CascadeType.ALL)
    List<UrlCheck> urlChecks;

    public Url(String urlName) {
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
