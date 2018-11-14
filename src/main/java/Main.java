import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import java.io.*;
import java.util.Optional;
import java.util.Stack;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists()) {
                processFile(file);
                return;
            }
            else {
                System.out.println("File not found");
                return;
            }
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            File file = new File(line);
            if (file.exists()) {
                System.out.println("PROCESSING "+file.toString());
                processFile(file);
                System.out.println("EOF");
                System.out.flush();
            }
            else {
                System.out.println("F_NOT_FOUND "+line);
                System.out.flush();
            }
        }
    }

    private static void processFile(File file) throws FileNotFoundException {
        int indexer = 0;
        Stack<TreeNode> stack = new Stack<>();

        CompilationUnit compilationUnit = JavaParser.parse(file);
        stack.push(new TreeNode(indexer++, compilationUnit));
        while (stack.size()>0) {
            TreeNode tnode = stack.pop();
            for (Node node: tnode.node.getChildNodes()) {
                Optional<Range> range = node.getRange();
                String strRange = "";
                if (range.isPresent()) {
                    strRange = recalculateRange(range.get(), file);
                }
                System.out.println("N:"+indexer+":"+tnode.id+":"+node.getMetaModel().getTypeNameGenerified()+":["+strRange+"]");
                stack.push(new TreeNode(indexer++, node));
            }
        }
    }

    private static String recalculateRange(Range range, File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int index = 0;
            int line = 1;
            int carry = 0;

            while (line < range.begin.line) {
                carry = 0;
                index++;
                char c = (char) reader.read();
                if (c == '\r') {
                    line++;
                    c = (char) reader.read();
                    index++;
                    if (c == '\n') {
                        //Nothing
                    }
                    else if (c == '\r') {
                        line++;
                    }
                    else {
                        carry = 1;
                    }
                }
                else if (c == '\n') {
                    line++;
                }
            }

            int beginIndex = index - carry + range.begin.column;
            while (line < range.end.line) {
                carry = 0;
                index++;
                char c = (char) reader.read();
                if (c == '\r') {
                    line++;
                    c = (char) reader.read();
                    index++;
                    if (c == '\n') {
                        //Nothing
                    }
                    else if (c == '\r') {
                        line++;
                    }
                    else {
                        carry = 1;
                    }
                }
                else if (c == '\n') {
                    line++;
                }
            }
            int endIndex = index - carry + range.end.column;

            return beginIndex + ":" + endIndex;

        } catch (IOException e) {
        }
        return "";
    }

    private static class TreeNode {
        public int id;
        public Node node;

        public TreeNode(int id, Node node) {
            this.id = id;
            this.node = node;
        }
    }
}
