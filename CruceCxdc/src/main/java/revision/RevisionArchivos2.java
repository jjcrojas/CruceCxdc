package revision;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RevisionArchivos2 {
    public static void main(String[] args) {
        // URL de conexión a Teradata
        String url = "jdbc:teradata://10.40.176.8/DATABASE=prod_dwh_sgp,USER=jcrojas,PASSWORD=JulPipe06*";
        String documento = "19350059"; // Asegúrate de reemplazar esto con el valor dinámico real

        // Cargar el driver JDBC de Teradata
        try {
            Class.forName("com.teradata.jdbc.TeraDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC de Teradata no encontrado");
            e.printStackTrace();
            return;
        }

        // Establecer la conexión
        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                System.out.println("Conectado a Teradata exitosamente");

                // Preparar y ejecutar la consulta SQL
                String query = "SELECT IDENTIFICACION, NOMBRES, CODIGO_ENTIDAD, DESC_CALIDAD_AFILPEN " +
                               "FROM afiliados_pensionados " +
                               "WHERE fecha_corte = '2024-08-31' AND identificacion = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    pstmt.setString(1, documento);
                    ResultSet rs = pstmt.executeQuery();

                    // Procesar los resultados
                    while (rs.next()) {
                        String identificacion = rs.getString("IDENTIFICACION");
                        String nombres = rs.getString("NOMBRES");
                        String codigoEntidad = rs.getString("CODIGO_ENTIDAD");
                        String descCalidadAfilpen = rs.getString("DESC_CALIDAD_AFILPEN");
                        System.out.println(identificacion + ", " + nombres + ", " + codigoEntidad + ", " + descCalidadAfilpen);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al conectarse a Teradata");
            e.printStackTrace();
        }
    }
}