package com.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.base.DBEntity;
import com.base.DBRecord;
import com.base.JDBCBase;
import com.base.TypeHandler;

/**
 * @author �뻪��
 */
public class AutoCreateEntityTool extends JDBCBase {
	private final String DBNAME = "invoicing_system";

	@Test
	public void Create() {
		// ��������
		String sql = "SELECT `table_name`, `column_name`, `data_type` FROM information_schema.columns WHERE table_schema = ?";
		Connection conn = super.getConnection();
		if (conn == null) {
			return;
		}
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			// ִ�����ݿ��ѯ
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, DBNAME);
			rs = pstmt.executeQuery();
			// ִ�д���ʵ��
			this.createClassFileCourse(rs);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			super.closeAll(conn, pstmt, rs);
		}
	}

	private void createClassFileCourse(ResultSet rs) {
		int createCount = 0;
		// ����ʵ����
		List<DBEntity> entities = null;
		try {
			entities = this.mapping(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// ��ʼ����entity���ļ�
		for (DBEntity entity : entities) {
			String content = this.produceClassContent(entity);
			if (content == null) {
				continue;
			}
			boolean createFileFlag = this.inputToFile(content, entity.getClassName());
			if (!createFileFlag) {
				continue;
			}
			createCount++;
		}

		System.out.println("�����ɹ�" + createCount + "���ļ�");
		System.out.println("����ʧ��" + (entities.size() - createCount) + "���ļ�");
	}

	private List<DBEntity> mapping(ResultSet rs) throws SQLException {
		Map<String, DBEntity> entities = new HashMap<>();
		// ���������
		while (rs.next()) {
			String tableName = rs.getString("table_name");
			// �жϵ�ǰ��¼�Ƿ��ǵ�һ�γ���
			if (entities.get(tableName) == null) {
				DBEntity entity = new DBEntity();
				// ȥ�������»��ߣ�Ȼ��ÿ�����ʿ�ͷ��д
				String[] nameBlock = tableName.toLowerCase().split("_");
				for (int i = 0; i < nameBlock.length; i++) {
					char[] nameChars = nameBlock[i].toCharArray();
					nameChars[0] -= 32;
					nameBlock[i] = new String(nameChars);
				}
				// ���ӱ���
				String className = String.join("", nameBlock);
				entity.setClassName(className);
				entity.setColums(new ArrayList<DBRecord>());
				entities.put(tableName, entity);
			}
			// ������¼��Ȼ�����ʵ��
			DBRecord record = new DBRecord();
			record.setTableName(tableName);
			// ȥ�������»���
			String[] propertyBlocks = rs.getString("column_name").split("_");
			propertyBlocks[0] = propertyBlocks[0].toLowerCase();
			if (propertyBlocks.length > 1) {
				for (int i = 1; i < propertyBlocks.length; i++) {
					propertyBlocks[i] = propertyBlocks[i].toLowerCase();
					char[] propertyChars = propertyBlocks[i].toCharArray();
					propertyChars[0] -= 32;
					propertyBlocks[i] = new String(propertyChars);
				}
			}
			String propertyName = String.join("", propertyBlocks);
			record.setColumnName(propertyName);
			record.setDataType(rs.getString("data_type"));
			Class<?> propertyClass = TypeHandler.getClassByHandler(rs.getString("data_type"));
			record.setPropertyClass(propertyClass);
			entities.get(tableName).getColums().add(record);
		}
		return new ArrayList<DBEntity>(entities.values());
	}

	private String produceClassContent(DBEntity entity) {
		// import��property��interface����
		List<String> imports = new ArrayList<>();
		List<String> properties = new ArrayList<>();
		List<String> interfaces = new ArrayList<>();

		for (DBRecord record : entity.getColums()) {
			Class<?> propertyClass = record.getPropertyClass();
			// ����import����
			if (!"java.lang".equals(propertyClass.getPackageName())) {
				imports.add("import " + propertyClass.getName() + ";");
			}

			// ����property����
			properties.add("private " + propertyClass.getSimpleName() + " " + record.getColumnName() + ";");

			// ����interface����
			char[] columnNameChars = record.getColumnName().toCharArray();
			columnNameChars[0] -= 32;
			String interfaceName = new String(columnNameChars);
			StringBuilder interfaceContent = new StringBuilder();
			interfaceContent
					.append("\tpublic " + propertyClass.getSimpleName() + " get" + interfaceName + "() {{enter}");
			interfaceContent.append("\t\treturn " + record.getColumnName() + ";{enter}");
			interfaceContent.append("\t}{enter}{enter}");
			interfaceContent.append("\tpublic void set" + interfaceName + "(" + propertyClass.getSimpleName() + " "
					+ record.getColumnName() + ") {{enter}");
			interfaceContent.append("\t\tthis." + record.getColumnName() + " = " + record.getColumnName() + ";{enter}");
			interfaceContent.append("\t}{enter}");
			interfaces.add(interfaceContent.toString());
		}

		// ����entity�ļ�����
		StringBuilder sb = new StringBuilder();
		sb.append("package com.entity;");
		sb.append("{enter}{enter}");
		for (String inport : imports) {
			sb.append(inport + "{enter}");
		}
		sb.append("{enter}");
		sb.append("/**{enter} * @author �뻪��{enter} */{enter}");
		sb.append("public class " + entity.getClassName() + " {{enter}");
		for (String property : properties) {
			sb.append("\t" + property + "{enter}");
		}
		sb.append("{enter}");
		for (String interfaze : interfaces) {
			sb.append(interfaze + "{enter}");
		}
		sb.append("}");
		return sb.toString().replace("{enter}", "\r\n");
	}

	private boolean inputToFile(String content, String className) {
		String packageName = "com.entity";
		packageName = packageName.replace(".", "/");
		File file = new File("src/" + packageName + "/" + className + ".java");
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		System.out.println(file.getAbsolutePath());
		FileOutputStream fop = null;
		OutputStreamWriter writer = null;
		try {
			fop = new FileOutputStream(file);
			writer = new OutputStreamWriter(fop, "GBK");
			writer.write(content);
			writer.close();
			fop.close();
		} catch (IOException e) {
			System.out.println(file.getName() + "����ʧ��\n");
			e.printStackTrace();
			return false;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fop != null) {
				try {
					fop.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println(file.getName() + "�����ɹ�\n");
		return true;
	}
}