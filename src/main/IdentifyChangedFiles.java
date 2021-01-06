package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;

import file.FileIOManager;
import jcodelib.diffutil.DiffParser;
import jcodelib.element.UnifiedHunk;
import jcodelib.jgit.ReposHandler;
import jcodelib.util.CodeUtils;

public class IdentifyChangedFiles {

	public static void main(String[] args) {
		String baseDir = "/Users/ihyeong-won/Desktop/SCD_Benchmark/SCD_Data";
		String revDir = "rev_info/";
		String[] projects = { "hadoop" };

		for (int i = 0; i < projects.length; i++) {
			String project = projects[i];
			String commitFileName = revDir + project + ".commits";
			System.out.println("Project - " + project);
			System.out.println("Base Dir - " + baseDir);
			System.out.println("Commit Ids - " + commitFileName);

			String oldReposPath = baseDir + File.separator + "old" + File.separator + project + File.separator;
			String newReposPath = baseDir + File.separator + "new" + File.separator + project + File.separator;
			File oldReposDir = new File(oldReposPath);
			File newReposDir = new File(newReposPath);
			Repository oldRepos = ReposHandler.getRepository(oldReposPath + ".git");

			HashMap<String, Set<String>> files = new HashMap<>();
			try {
				List<String> commitIds = readFile(commitFileName);
				System.out.println("Total " + commitIds.size() + " revisions.");
				StringBuffer sb = new StringBuffer();
				for (String strCommits : commitIds) {
					// Reset hard to old/new commit IDs.
					String[] tokens = strCommits.split(",");
					String oldCommitId = tokens[1];
					String newCommitId = tokens[2];
					System.out.println("Processing " + strCommits);
					ReposHandler.update(oldReposDir, oldCommitId);
					ReposHandler.update(newReposDir, newCommitId);
					System.out.println("Update Done.");
					String diff = ReposHandler.getDiff(oldRepos, oldCommitId, newCommitId);
					List<UnifiedHunk> hunks = DiffParser.parseUnitifedDiff(diff);
					System.out.println("Total " + hunks.size() + " hunks.");
					Set<String> changedFiles = new HashSet<>();
					for (UnifiedHunk hunk : hunks) {
						if (hunk.isJava() && hunk.isChanged() && !changedFiles.contains(hunk.oldFileName)) {
							String f = hunk.oldFileName;
							if (!f.endsWith("package-info.java")) {
								File oldFile = new File(oldReposPath + hunk.oldFileName);
								File newFile = new File(newReposPath + hunk.newFileName);
								if (hasCodeChanges(hunk, oldFile, newFile)) {
									changedFiles.add(hunk.oldFileName);
								}
							}
						}
					}
					System.out.println("Total " + changedFiles.size() + " changed files.");
					if (changedFiles.size() > 0) {
						files.put(strCommits, changedFiles);
						sb.append(strCommits);
						sb.append("\n");
					}
				}
				FileIOManager.writeObject(revDir + project + ".changedfiles.obj", files);
				FileIOManager.writeContent(revDir + project + ".targets", sb.toString());

			} catch (NoHeadException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static List<String> readFile(String commitFileName) throws IOException {
		String content = FileIOManager.getContent(new File(commitFileName));
		String[] lines = content.split("\n");
		List<String> list = Arrays.asList(lines);
		return list;
	}

	private static boolean hasCodeChanges(UnifiedHunk hunk, File oldFile, File newFile) {
		Set<Integer> oldCommentLineNumbers = CodeUtils.getCommentLineNumbers(oldFile);
		Set<Integer> newCommentLineNumbers = CodeUtils.getCommentLineNumbers(newFile);
		Set<Integer> added = new HashSet<>(hunk.addedLines.keySet());
		Set<Integer> deleted = new HashSet<>(hunk.deletedLines.keySet());
		added.removeAll(newCommentLineNumbers);
		deleted.removeAll(oldCommentLineNumbers);
		return added.size() > 0 || deleted.size() > 0;
	}

}
