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
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RevisionArchivos5 {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis(); // Capturar el tiempo de inicio

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
		List<String> documentos = leerDocumentosDeExcel(
				"D:\\OneDrive - Superfinanciera\\Pensiones\\Caxdac\\Skandia\\skandiaASfc2.xlsx");

		// Crear un libro de trabajo de Excel para los resultados
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Resultados");
		// Establecer la conexión
		try (Connection connection = DriverManager.getConnection(url)) {
			if (connection != null) {
				System.out.println("Conectado a Teradata exitosamente");

				// Preparar la consulta SQL
				String query = "SELECT IDENTIFICACION, NOMBRES, APELLIDOS, CODIGO_ENTIDAD, DESC_CALIDAD_AFILPEN "
						+ "FROM afiliados_pensionados "
						+ "WHERE fecha_corte = '2024-08-31' AND TIPO_ENTIDAD = 23 AND CODIGO_ENTIDAD = 9 AND identificacion = ?";
				int rowNum = 0;
				for (String documento : documentos) {
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, documento);
						ResultSet rs = pstmt.executeQuery();

						// Procesar los resultados
						while (rs.next()) {
							Row row = sheet.createRow(rowNum++);
							row.createCell(0).setCellValue(rs.getString("IDENTIFICACION"));
							row.createCell(1).setCellValue(rs.getString("NOMBRES"));
							row.createCell(2).setCellValue(rs.getString("APELLIDOS"));
							row.createCell(3).setCellValue(rs.getString("CODIGO_ENTIDAD"));
							row.createCell(4).setCellValue(rs.getString("DESC_CALIDAD_AFILPEN"));
						}
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error al conectarse a Teradata");
			e.printStackTrace();
		} finally {
			// Guardar el archivo Excel
			try (FileOutputStream out = new FileOutputStream(new File("D:\\OneDrive - Superfinanciera\\Pensiones\\Caxdac\\Skandia\\Resultados.xlsx"))) {
				workbook.write(out);
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			long endTime = System.currentTimeMillis(); // Capturar el tiempo de finalización
			long duration = endTime - startTime; // Calcular la duración
			System.out.println("Ejecución finalizada.");
			// Convertir la duración de milisegundos a horas, minutos y segundos
			long segundos = (duration / 1000) % 60;
			long minutos = (duration / (1000 * 60)) % 60;
			long horas = (duration / (1000 * 60 * 60)) % 24;

			String tiempoFormateado = String.format("%02d horas, %02d minutos, %02d segundos", horas, minutos,
					segundos);
			System.out.println("Duración del proceso: " + tiempoFormateado);

		}
	}

	public static List<String> leerDocumentosDeExcel(String filePath) {
		List<String> documentos = new ArrayList<>();
		try (FileInputStream file = new FileInputStream(new File(filePath));
				Workbook workbook = new XSSFWorkbook(file)) {
			Sheet sheet = workbook.getSheetAt(0);
			for (Row row : sheet) {
				// Comenzar a leer desde la fila 2, saltando el encabezado
				if (row.getRowNum() < 1) { // Esto ignora la primera fila (encabezado)
					continue; // Salta al siguiente ciclo del bucle for
				}
				Cell cell = row.getCell(1); // Asume que los documentos están en la columna 2
				if (cell != null) {
					switch (cell.getCellType()) {
					case STRING:
						String stringValue = cell.getStringCellValue();
						documentos.add(stringValue);
						break;
					case NUMERIC:
						double numericValue = cell.getNumericCellValue();
						// Usar BigDecimal para evitar la notación científica
						String stringValueN = new BigDecimal(numericValue).toPlainString();
						documentos.add(stringValueN);
						break;
					// Puedes manejar otros tipos de celdas (BOOLEAN, FORMULA, BLANK, etc.) de
					// manera similar
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return documentos;
	}
}