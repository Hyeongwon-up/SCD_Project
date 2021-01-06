package main;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import db.DBManager;
import file.FileIOManager;
import jcodelib.diffutil.TreeDiff;
import jcodelib.element.GTAction;
import jcodelib.jgit.ReposHandler;

public class DiffExample {

	public static final int GUMTREE = 1;
	public static final int CHANGE_DISTILLER = 2;
	public static final int LAS = 3;
	public static final int CLDiff = 4;

	public static String[] projects = { "hadoop" };
	public static String baseDir = "/Users/ihyeong-won/Desktop/SCD_Benchmark/SCD_Data";

	public static void main(String[] args) {
		// Select SCD Tool here.
		int tool = GUMTREE;

		final boolean insertFileInfo = true;
		DBManager db = null;
		try {
			db = new DBManager("db.properties"); // db.properties 파일에 저장된 접속정보를 읽어옴.
			Connection con = db.getConnection();
			PreparedStatement psIns = con.prepareStatement(
					"insert into changes2 ( fileId, tool, change_type, entity_type, edit ) values ( ?, ?, ?, ?, ? )");
			PreparedStatement errcol = con.prepareStatement("insert into err_ver2 (fileId) values(?) ");
			for (int x = 0; x < projects.length; x++) {
				String project = projects[x];
				PreparedStatement ps = con.prepareStatement(
						"insert into files_ver2 (project, issue_id, old_commitid, new_commitid, file) " + "values ('"
								+ project + "', ?, ?, ?, ?)");

				System.out.println("Collecting Changes from " + project);
				String oldReposPath = baseDir + File.separator + "old" + File.separator + project + File.separator;
				String newReposPath = baseDir + File.separator + "new" + File.separator + project + File.separator;
				File oldReposDir = new File(oldReposPath);
				File newReposDir = new File(newReposPath);
				List<String> commitIds = getCommitIds("rev_info/" + project + ".targets");
				HashMap<String, Set<String>> changedFiles = (HashMap<String, Set<String>>) FileIOManager
						.getObject("rev_info/" + project + ".changedfiles.obj");
				System.out.println("Total " + commitIds.size() + " revisions.");
				for (int i = 0; i < commitIds.size(); i++) {
					// Reset hard to old/new commit IDs.
					String key = commitIds.get(i);
					String[] tokens = key.split(",");
					String issueId = tokens[0];
					String oldCommitId = tokens[1];
					String newCommitId = tokens[2];
					System.out.println("Commit " + i + " - " + oldCommitId + " " + newCommitId);
					List<String> files = changedFiles.containsKey(key) ? new ArrayList<String>(changedFiles.get(key))
							: new ArrayList<String>();
					if (files.size() == 0) {
						System.out.println("No changed files.");
						continue;
					}
					ReposHandler.update(oldReposDir, oldCommitId);
					ReposHandler.update(newReposDir, newCommitId);
					System.out.println("Processing " + files.size() + " files.");
					String fileId = null;
					for (int j = 0; j < files.size(); j++) {

						try {
							String f = files.get(j);
							if (f.indexOf("/org/") < 0)
								continue;
							System.out.println(f);
							File oldFile = new File(oldReposPath + f);
							File newFile = new File(newReposPath + f);
							String oldCode = FileIOManager.getContent(oldFile).intern();
							String newCode = FileIOManager.getContent(newFile).intern();
							if (oldCode.length() == 0 || newCode.length() == 0) {
								// Practically these files are deleted/inserted.
								continue;
							}

							// Insert file info for statistics.
							if (insertFileInfo) {
								ps.clearParameters();
								ps.setString(1, issueId);
								ps.setString(2, oldCommitId.substring(0, 8));
								ps.setString(3, newCommitId.substring(0, 8));
								ps.setString(4, f);
								ps.addBatch();
							}

							// Apply Source Code Differencing Tools.
							fileId = issueId + "_" + newCommitId.substring(0, 7) + ":" + f;
							if (tool == GUMTREE) {
								List<GTAction> gumtreeChanges = TreeDiff.diffGumTreeWithGrouping(oldFile, newFile);
								for (GTAction c : gumtreeChanges) {
									// tool 사이에 출력이 일치하도록 이 부분을 수정.
									psIns.clearParameters();
									psIns.setString(1, fileId);
									psIns.setInt(2, tool);
									psIns.setString(3, c.actionType);
									psIns.setString(4, c.codeType);
									psIns.setString(5, c.toString());
									psIns.addBatch();
								}
							} else if (tool == CHANGE_DISTILLER) {
								List<SourceCodeChange> changes = TreeDiff.diffChangeDistiller(oldFile, newFile);
								for (SourceCodeChange c : changes) {
									// tool 사이에 출력이 일치하도록 이 부분을 수정.
									psIns.clearParameters();
									psIns.setString(1, fileId);
									psIns.setInt(2, tool);
									psIns.setString(3, ConvertCDOutputChange(c.toString()));
									psIns.setString(4, ConvertCDOutputEntity(c.getChangedEntity().toString()));
									/*
									 * psIns.setString(3, c.getChangeType().toString()); psIns.setString(4,
									 * c.getChangedEntity().toString());
									 */

									psIns.setString(5, c.toString());
									psIns.addBatch();
								}
							} else if (tool == LAS) {
								// Don't use this part yet.
							} else if (tool == CLDiff) {

							}
						} catch (Exception e) {
							System.out.println("Error while processing " + fileId);
							errcol.clearParameters();
							errcol.setString(1, fileId);
							e.printStackTrace();
							errcol.addBatch();
						}
					}
					ps.executeBatch();
					ps.clearBatch();
					psIns.executeBatch();
					psIns.clearBatch();
					errcol.executeBatch();
					errcol.clearBatch();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}

	private static String ConvertCDOutputEntity(String string) {

		String[] first = string.split(":");
		String[] second = first[0].split("_");

		if (second.length == 1) {
			String front = second[0].substring(0, 1).toUpperCase() + second[0].substring(1).toLowerCase();

			return string = front;
		}
		if (second.length == 2) {
			String front = second[0].substring(0, 1).toUpperCase() + second[0].substring(1).toLowerCase();
			String back = second[1].substring(0, 1).toUpperCase() + second[1].substring(1).toLowerCase();

			return string = front + back;

		}

		if (second.length == 3) {
			String front = second[0].substring(0, 1).toUpperCase() + second[0].substring(1).toLowerCase();
			String back = second[1].substring(0, 1).toUpperCase() + second[1].substring(1).toLowerCase();
			String back2 = second[2].substring(0, 1).toUpperCase() + second[2].substring(1).toLowerCase();

			return string = front + back + back2;

		}

		else
			return string;

	}

	private static String ConvertCDOutputChange(String string) {

		String[] s1 = string.split(":");
		String s2 = s1[0].substring(0, 3).toUpperCase();

		return string = s2;

	}

	private static List<String> getCommitIds(String commitIdFileName) throws IOException {
		String content = FileIOManager.getContent(new File(commitIdFileName));
		String[] commits = content.split("\n");
		List<String> list = Arrays.asList(commits);
		return list;
	}

}
