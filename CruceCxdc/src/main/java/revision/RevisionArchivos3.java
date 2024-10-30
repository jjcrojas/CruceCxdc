package revision;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class RevisionArchivos3 {
    public static void main(String[] args) {
        String url = "jdbc:teradata://10.40.176.8/DATABASE=prod_dwh_sgp,USER=jcrojas,PASSWORD=JulPipe06*";
        
        // Cargar el driver JDBC de Teradata
        try {
            Class.forName("com.teradata.jdbc.TeraDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver JDBC de Teradata no encontrado");
            e.printStackTrace();
            return;
        }
        
        // Leer documentos desde el archivo Excel
        List<String> documentos = leerDocumentosDeExcel("D:\\OneDrive - Superfinanciera\\Pensiones\\Caxdac\\Skandia\\skandiaASfc2.xlsx");
        
        // Establecer la conexión
        try (Connection connection = DriverManager.getConnection(url)) {
            if (connection != null) {
                System.out.println("Conectado a Teradata exitosamente");
                
                // Preparar la consulta SQL
                String query = "SELECT IDENTIFICACION, NOMBRES, CODIGO_ENTIDAD, DESC_CALIDAD_AFILPEN " +
                               "FROM afiliados_pensionados " +
                               "WHERE fecha_corte = '2024-08-31' AND identificacion = ?";
                
                for (String documento : documentos) {
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
            }
        } catch (SQLException e) {
            System.out.println("Error al conectarse a Teradata");
            e.printStackTrace();
        }
    }
    
    public static List<String> leerDocumentosDeExcel(String filePath) {
        List<String> documentos = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Cell cell = row.getCell(1); // Asume que los documentos están en la columna 2
                if (cell != null && row.getRowNum() > 0) { // Ignora el encabezado
                    documentos.add(cell.getStringCellValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentos;
    }
}
