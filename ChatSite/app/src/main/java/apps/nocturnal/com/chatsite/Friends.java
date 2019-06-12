package apps.nocturnal.com.chatsite;

public class Friends {

    public String name,image,friendship,status;

    public Friends(String name, String image, String friendship, String status) {
        this.name = name;
        this.image = image;
        this.friendship = friendship;
        this.status = status;
    }

    public Friends(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFriendship() {
        return friendship;
    }

    public void setFriendship(String friendship) {
        this.friendship = friendship;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
