package revision;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RevisionArchivos9 {
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

		// Leer documentos y cabeceras desde el archivo Excel
		List<String> documentos = new ArrayList<>();
		List<String> cabeceras = new ArrayList<>();
		Map<String, Integer> documentoRowMap = new HashMap<>();
		Workbook inputWorkbook = leerDocumentosDeExcel(
				"D:\\OneDrive - Superfinanciera\\Pensiones\\Caxdac\\Skandia\\skandiaASfc2.xlsx", documentos, cabeceras,
				documentoRowMap);

		// Crear un libro de trabajo de Excel para los resultados
		Workbook outputWorkbook = new XSSFWorkbook();
		Sheet outputSheet = outputWorkbook.createSheet("Resultados");

		// Llamar al método copiarFilas para copiar las filas del archivo de entrada al
		// archivo de salida
		copiarFilas(inputWorkbook, outputWorkbook);

		
		
		// Establecer la conexión
		try (Connection connection = DriverManager.getConnection(url)) {
			if (connection != null) {
				System.out.println("Conectado a Teradata exitosamente");

				List<String> cabecerasConsulta = Arrays.asList("IDENTIFICACION", "NOMBRES", "APELLIDOS", "CODIGO_ENTIDAD", "DESC_CALIDAD_AFILPEN");
				agregarCabecerasConsulta(outputSheet, cabecerasConsulta);
				
				// Preparar la consulta SQL
				String query = "SELECT IDENTIFICACION, NOMBRES, APELLIDOS, CODIGO_ENTIDAD, DESC_CALIDAD_AFILPEN "
						+ "FROM afiliados_pensionados "
						+ "WHERE fecha_corte = '2024-08-31' AND TIPO_ENTIDAD = 23 AND CODIGO_ENTIDAD = 9 AND identificacion = ?";
				for (String documento : documentos) {
					try (PreparedStatement pstmt = connection.prepareStatement(query)) {
						pstmt.setString(1, documento);
						ResultSet rs = pstmt.executeQuery();

						boolean documentoEncontrado = false; // Variable para rastrear si se encontraron filas para el
																// documento

						// Procesar los resultados
						while (rs.next()) {
							documentoEncontrado = true; // Marcar como encontrado
							if (documentoRowMap.containsKey(documento)) {
								int rowNum = documentoRowMap.get(documento);
								Row row = outputSheet.getRow(rowNum);
								if (row == null) {
									row = outputSheet.createRow(rowNum); // Crear la fila si no existe
								}
								int lastCellNum = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum(); 
								row.createCell(lastCellNum++).setCellValue(rs.getString("IDENTIFICACION"));
								row.createCell(lastCellNum++).setCellValue(rs.getString("NOMBRES"));
								row.createCell(lastCellNum++).setCellValue(rs.getString("APELLIDOS"));
								row.createCell(lastCellNum++).setCellValue(rs.getString("CODIGO_ENTIDAD"));
								row.createCell(lastCellNum).setCellValue(rs.getString("DESC_CALIDAD_AFILPEN"));
							}
						}
						// Si después de procesar la consulta, documentoEncontrado es falso, entonces el
						// documento no fue encontrado
						if (!documentoEncontrado) {
							int rowNum = documentoRowMap.get(documento);
							Row row = outputSheet.getRow(rowNum);
							if (row == null) {
								row = outputSheet.createRow(rowNum);
							}
							int lastCellNum = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
							row.createCell(lastCellNum).setCellValue("Documento no encontrado");
						}
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Error al conectarse a Teradata");
			e.printStackTrace();
		} finally {
			// Guardar el archivo Excel
			try (FileOutputStream out = new FileOutputStream(
					new File("D:\\OneDrive - Superfinanciera\\Pensiones\\Caxdac\\Skandia\\Resultados.xlsx"))) {
				outputWorkbook.write(out);
				outputWorkbook.close();
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

	public static Workbook leerDocumentosDeExcel(String filePath, List<String> documentos, List<String> cabeceras,
			Map<String, Integer> documentoRowMap) {
		Workbook workbook = null;
		try (FileInputStream file = new FileInputStream(new File(filePath))) {
			workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);
			Row firstRow = sheet.getRow(0);
			for (Cell cell : firstRow) {
				cabeceras.add(cell.getStringCellValue());
			}
			for (Row row : sheet) {
				if (row.getRowNum() < 1) {
					continue; // Salta la primera fila (encabezado)
				}
				Cell cell = row.getCell(1); // Asume que los documentos están en la columna 2
				if (cell != null) {
					String documento = "";
					switch (cell.getCellType()) {
					case STRING:
						documento = cell.getStringCellValue();
						documentos.add(documento);
						break;
					case NUMERIC:
						documento = new BigDecimal(cell.getNumericCellValue()).toPlainString();
						documentos.add(documento);
						break;
					default:
						break;
					}
					documentoRowMap.put(documento, row.getRowNum());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workbook;
	}

	public static void copiarFilas(Workbook inputWorkbook, Workbook outputWorkbook) {
		Sheet inputSheet = inputWorkbook.getSheetAt(0);
		Sheet outputSheet = outputWorkbook.getSheet("Resultados");

		// Verificar si la hoja ya existe y, si no, crear una nueva
		if (outputSheet == null) {
			outputSheet = outputWorkbook.createSheet("Resultados");
		}

		// Crear un mapa para almacenar los estilos ya copiados y reutilizarlos
		Map<CellStyle, CellStyle> styleMap = new HashMap<>();

		// Iterar sobre todas las filas del inputSheet
		for (int i = 0; i <= inputSheet.getLastRowNum(); i++) {
			Row srcRow = inputSheet.getRow(i);
			Row destRow = outputSheet.createRow(i);

			// Asegurarse de que la fila existe
			if (srcRow != null) {
				// Iterar sobre todas las celdas de la fila actual
				for (int j = srcRow.getFirstCellNum(); j < srcRow.getLastCellNum(); j++) {
					Cell srcCell = srcRow.getCell(j);
					Cell destCell = destRow.createCell(j);

					// Copiar el contenido de la celda, asegurándose de que la celda existe
					if (srcCell != null) {
						// Copiar el contenido de la celda
						switch (srcCell.getCellType()) {
						case STRING:
							destCell.setCellValue(srcCell.getStringCellValue());
							break;
						case NUMERIC:
							destCell.setCellValue(srcCell.getNumericCellValue());
							break;
						case BOOLEAN:
							destCell.setCellValue(srcCell.getBooleanCellValue());
							break;
						case FORMULA:
							destCell.setCellFormula(srcCell.getCellFormula());
							break;
						case BLANK:
							destCell.setCellType(CellType.BLANK);
							break;
						case ERROR:
							destCell.setCellErrorValue(srcCell.getErrorCellValue());
							break;
						default:
							break;
						}

						// Copiar el estilo de la celda, creando un nuevo estilo en el libro de salida
						// si es necesario
						CellStyle originalStyle = srcCell.getCellStyle();
						CellStyle newStyle = styleMap.get(originalStyle);
						if (newStyle == null) {
							newStyle = outputWorkbook.createCellStyle();
							newStyle.cloneStyleFrom(originalStyle);
							styleMap.put(originalStyle, newStyle);
						}
						destCell.setCellStyle(newStyle);
					}
				}
			}
		}
	}
	// Añadir cabeceras de la consulta SQL a la primera fila del archivo de salida
	public static void agregarCabecerasConsulta(Sheet outputSheet, List<String> cabecerasConsulta) {
	    Row headerRow = outputSheet.getRow(0); // Obtener la primera fila para las cabeceras
	    if (headerRow == null) {
	        headerRow = outputSheet.createRow(0); // Crear la fila si no existe
	    }
	    int firstCellNum = headerRow.getLastCellNum() == -1 ? 0 : headerRow.getLastCellNum(); // Ajuste para cuando getLastCellNum() devuelve -1
	    for (String cabecera : cabecerasConsulta) {
	        headerRow.createCell(firstCellNum++).setCellValue(cabecera);
	    }
	}	
}