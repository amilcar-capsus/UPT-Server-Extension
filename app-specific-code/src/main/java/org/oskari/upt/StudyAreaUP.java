package org.oskari.upt;

public class StudyAreaUP {

    public Integer id;
    public String name;

    public StudyAreaUP() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "{" +
                "\"id\":\"" + id.toString() + '"' +
                ", \"name\":'" + name + '"' +
                '}';
    }
}