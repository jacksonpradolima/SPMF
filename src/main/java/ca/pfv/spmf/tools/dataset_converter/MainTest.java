package ca.pfv.spmf.tools.dataset_converter;

public class MainTest {
	public static void main(String[] args) {
		String test = "\u4E2D.,.,!?\u3002CD";
		System.out.println(test.replaceAll("[^\\p{L}\\p{N}]+", "").toLowerCase());
	}
}
