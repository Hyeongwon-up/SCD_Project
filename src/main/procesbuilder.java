package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class procesbuilder {

	public static void main(String[] args) {

		Runtime rt = Runtime.getRuntime();
		StringBuffer HW = new StringBuffer();
		String result = "";
		List<String> cmd = new ArrayList<String>();
		cmd.add("diff -y --suppress-common-lines");
		cmd.add("-y");
		cmd.add("--suppress-common-lines");
		cmd.add("/Users/ihyeong-won/Desktop/SCD_Benchmark_project/SCDBenchmark2/src/main/oldtest.java");
		cmd.add("/Users/ihyeong-won/Desktop/SCD_Benchmark_project/SCDBenchmark2/src/main/newtest.java");
		cmd.add("|");
		cmd.add("wc");
		cmd.add("-l");
		String joined = String.join(" ", cmd);
		String[] cmd2 = { "/bin/sh", "-c", joined };

		try {
			Process proc = rt.exec(cmd2);
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String cl = null;
			while ((cl = in.readLine()) != null) {
				HW.append(cl);
			}
			result = HW.toString();

			in.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
		System.out.println(result);

	}

}