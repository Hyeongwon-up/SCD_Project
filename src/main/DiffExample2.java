package main;

import java.io.File;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import jcodelib.diffutil.TreeDiff;

public class DiffExample2 {

	public static void main(String[] args) {
		File before = new File("resource/MAX_CHD/old_max_chd.java");
		File after = new File("resource/MAX_CHD/new_max_chd.java");

		String oldRepoPath = "/Volumes/Data/scdbenchmark/subjects/old/lang";
		String newRepoPath = "/Volumes/Data/scdbenchmark/subjects/new/lang";
		String fileName = "src/main/java/org/apache/commons/lang3/builder/ToStringStyle.java";
		before = new File(oldRepoPath + File.separator + fileName);
		after = new File(newRepoPath + File.separator + fileName);

		// ChangeDistiller
		List<SourceCodeChange> changes = TreeDiff.diffChangeDistiller(before, after);
		for (SourceCodeChange c : changes) {
			System.out.println(c);
			System.out.println("Type:" + c.getChangeType());
			System.out.println("EntityType:" + c.getChangedEntity());
		}
	}

}
