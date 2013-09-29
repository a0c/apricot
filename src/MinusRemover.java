import java.io.*;

/**
 * @author Anton Chepurov
 */
public class MinusRemover {
	private final String filePath;

	public MinusRemover(String filePath) {
		this.filePath = filePath;
	}

	private void remove() throws IOException {
		File file = new File(filePath);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file), 2000000);
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(file.getParentFile(), file.getName() + ".DONE")), 2000000);
		String line;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.startsWith(".") || line.length() == 0 || !line.startsWith("-")) {
				bufferedWriter.write(line);
				bufferedWriter.newLine();
				continue;
			}

			if (line.startsWith("-")) {
				String[] numbers = line.split(" ");
				StringBuilder sb = new StringBuilder();
				for (String number : numbers) {
					int numberI = Integer.parseInt(number);
					if (numberI < 0) {
						numberI = 256 + numberI;
					}
					sb.append(numberI).append(" ");
				}
				sb.delete(sb.length() - 1, sb.length());
				bufferedWriter.write(sb.toString());
				bufferedWriter.newLine();
				continue;
			}

			throw new RuntimeException("Cannot process this line: " + line);
		}
		bufferedWriter.close();
		bufferedReader.close();
	}

	public static void main(String[] args) throws IOException {
		MinusRemover minusRemover = new MinusRemover("Desktop/TTU temp/Elsevier IST paper/Designs/For parse/DONE/Simul/convert_no_ph1_testmode_input/TOTAL_M/hc11rtl_edit2_M_DH_POS.tst");
		minusRemover.remove();
	}
}
