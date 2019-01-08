import com.github.javaparser.Position;

import java.io.*;
import java.util.ArrayList;

public class FileMap {

    private File file;
    private ArrayList<Integer> lineOffsets;

    public FileMap(File file) throws FileNotFoundException {
        this.file = file;
        this.lineOffsets = new ArrayList<>();
        this.lineOffsets.add(0);
        this.lineOffsets.add(0); //Lines are counted from first to last
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
            int index = 0;
            while (reader.ready()) {
                char c = (char) reader.read();
                index++;
                if (c < 0) break;
                if (c == '\r') {
                    this.lineOffsets.add(index);
                    c = (char) reader.read();
                    index++;
                    if (c == '\n') {
                        this.lineOffsets.set(this.lineOffsets.size()-1, index);
                    } else if (c == '\r') {
                        this.lineOffsets.add(index);
                    }
                } else if (c == '\n') {
                    this.lineOffsets.add(index);
                }
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            //Probably do nothing
        }
    }

    public File getFile() {
        return file;
    }

    public int getOffset(Position position) {
        return this.lineOffsets.get(position.line) + position.column;
    }
}
