package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import file.FileIOManager;
import jcodelib.jgit.ReposHandler;

public class ExportFilePairs {

	public static Map<String, String> oldCommitIds = new HashMap<>();
	public static Map<String, String> newCommitIds = new HashMap<>();
	public static String[] projects = { "lang", "math", "collections", "ivy", "hadoop", "hbase", "derby" };

	public static void main(String[] args) {
		String baseDir = "/Users/ihyeong-won/Desktop/data";
		String outputDir = "/Users/ihyeong-won/Desktop/same";
		String inputFileName = "test_list"; // path to a file with <fileId>, one fileId for each line.

		try {
			loadCommitIds();
			String content = FileIOManager.getContent(new File(inputFileName));
			String[] lines = content.split("\n");
			List<FileInfo> files = new ArrayList<>();
			for (String fileId : lines) {
				files.add(new FileInfo(fileId));
			}

			System.out.println("Copying Files...");
			int count = 0;
			for (FileInfo file : files) {
				try {
					String oldPath = file.repoPath(baseDir, true);
					String newPath = file.repoPath(baseDir, false);
					ReposHandler.update(new File(oldPath), file.oldCommitId);
					ReposHandler.update(new File(newPath), file.newCommitId);
					File dir = new File(outputDir + File.separator + "file" + count++);
					if (dir.mkdirs()) {
						FileIOManager.copyFile(new File(oldPath + file.filePath),
								new File(dir.getAbsolutePath() + File.separator + "old_2~!"));
						FileIOManager.copyFile(new File(newPath + file.filePath),
								new File(dir.getAbsolutePath() + File.separator + "new_2"));
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Done.");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadCommitIds() throws IOException {
		System.out.println("Loading commit IDs...");
		for (String prj : projects) {
			String content = FileIOManager.getContent(new File("rev_info/" + prj + ".targets"));
			String[] lines = content.split("\n");
			for (String line : lines) {
				String[] tokens = line.split(",");
				String issueId = tokens[0];
				oldCommitIds.put(issueId, tokens[1]);
				newCommitIds.put(issueId, tokens[2]);
			}
		}
		System.out.println("Done.");
	}

	private static class FileInfo {
		public String project;
		public String oldCommitId;
		public String newCommitId;
		public String filePath;
		public String fileId;

		public FileInfo(String fileId) {
			String issueId = fileId.substring(0, fileId.indexOf("_"));
			project = issueId.substring(0, issueId.indexOf("-")).toLowerCase();
			filePath = fileId.substring(fileId.indexOf(":") + 1);
			oldCommitId = oldCommitIds.get(issueId);
			newCommitId = newCommitIds.get(issueId);
		}

		public String repoPath(String baseDir, boolean isOld) {
			return baseDir + File.separator + (isOld ? "old" : "new") + File.separator + project + File.separator;
		}
	}

}
