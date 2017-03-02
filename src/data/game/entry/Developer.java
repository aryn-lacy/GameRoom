package data.game.entry;

import data.io.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by LM on 02/03/2017.
 */
public class Developer {
    private int igdb_id = -1;
    private String name;
    private boolean id_needs_update;

    public Developer(int igdb_id, String name) {
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("Developper's name was either null or empty : \""+name+"\"");
        }
        this.igdb_id = igdb_id;
        this.name = name;
    }

    public Developer(String name) {
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("Developper's name was either null or empty : \""+name+"\"");
        }
        this.name = name;
    }

    public int insertInDB(){
        try {
            String sql = igdb_id < 0 ? "INSERT OR IGNORE INTO Developer(name,id_needs_update) VALUES (?,?)" : "INSERT OR IGNORE INTO Developer(igdb_id,name) VALUES (?,?)";
            PreparedStatement devStatement = DataBase.getConnection().prepareStatement(sql);
            devStatement.setString(1, name);
            if(igdb_id >= 0){
                devStatement.setInt(2,igdb_id);
            }else{
                devStatement.setInt(2,1);
            }
            devStatement.execute();
            devStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        igdb_id = getIdInDb();
        return igdb_id;
    }

    private int getIdInDb(){
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("Developper's name was either null or empty : \""+name+"\"");
        }
        int id = -1;
        try {
            Connection connection = DataBase.getConnection();
            PreparedStatement getIdQuery = connection.prepareStatement("SELECT igdb_id FROM Developer WHERE name = ?");
            getIdQuery.setString(1,name);
            ResultSet result = getIdQuery.executeQuery();

            if(result.next()){
                id = result.getInt(1);
                result.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return id;
    }
}
