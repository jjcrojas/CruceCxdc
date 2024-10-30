package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:teradata://10.40.176.8/DATABASE=prod_dwh_sgp,USER=jcrojas,PASSWORD=JulPipe06*";

    public static Connection obtenerConexion() {
        try {
            Class.forName("com.teradata.jdbc.TeraDriver");
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC de Teradata no encontrado");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error al conectarse a Teradata");
            e.printStackTrace();
        }
        return null;
    }
}
