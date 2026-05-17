package com.flower;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.flower.ast.BinaryExpr;
import com.flower.ast.Expression;
import com.flower.ast.LiteralExpr;
import com.flower.ast.VarExpr;

public class MiniFlower {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Использование:");
            System.out.println("  java -jar mnflw.jar <путь_к_файлу.class или .jar>");
            return;
        }

        String targetPath = args[0];

        if (targetPath.equalsIgnoreCase("--easter-egg")) {
            showEasterEgg();
            return;
        }

        File file = new File(targetPath);
        if (!file.exists()) {
            System.err.println("Ошибка: Файл не найден по пути: " + targetPath);
            return;
        }

        if (targetPath.endsWith(".jar")) {
            processJarFile(file);
        } else if (targetPath.endsWith(".class")) {
            processClassFile(file);
        } else {
            System.err.println("Ошибка: Поддерживаются только файлы .class или .jar");
        }
    }

    private static void processClassFile(File file) {
        String outPath = file.getAbsolutePath().replaceAll("\\.class$", ".java");
        File outFile = new File(outPath);

        System.out.println("Декомпиляция класса в файл: " + outFile.getName());

        try (PrintWriter writer = new PrintWriter(new FileWriter(outFile))) {
            printHeader(writer);
            try (FileInputStream fis = new FileInputStream(file)) {
                decompileStream(fis, writer);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи файла класса: " + e.getMessage());
        }
    }

    private static void processJarFile(File file) {
        String outputDirName = file.getName().replaceAll("\\.jar$", "") + "_decompiled";
        File outputDir = new File(file.getParent(), outputDirName);

        System.out.println("Декомпиляция JAR в папку: " + outputDir.getAbsolutePath());

        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String javaRelativePath = entry.getName().replaceAll("\\.class$", ".java");
                    File javaFile = new File(outputDir, javaRelativePath);

                    File parentDir = javaFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }

                    try (PrintWriter writer = new PrintWriter(new FileWriter(javaFile)); InputStream is = jar.getInputStream(entry)) {

                        printHeader(writer);
                        decompileStream(is, writer);
                    }
                }
            }
            System.out.println("Готово! Все файлы сохранены.");
        } catch (IOException e) {
            System.err.println("Ошибка при чтении JAR файла: " + e.getMessage());
        }
    }

    private static void printHeader(PrintWriter writer) {
        writer.println("// ----------------------------------------------------");
        writer.println("// Decompiled by MiniFlower Pro v1.0");
        writer.println("// ----------------------------------------------------");
    }

    private static void decompileStream(InputStream is, PrintWriter writer) throws IOException {
        ClassReader reader = new ClassReader(is);
        reader.accept(new DecompilerClassVisitor(writer), 0);
    }

    private static void showEasterEgg() {
        System.out.println("Запуск секретного графического модуля...");

        JFrame frame = new JFrame("Miniflower Secret Easter Egg");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 420);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            private float hue = 0.0f;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                int centerX = width / 2;
                int centerY = height / 2 - 20;

                g2d.setColor(new Color(25, 25, 25));
                g2d.fillRect(0, 0, width, height);

                g2d.setColor(new Color(46, 139, 87));
                g2d.setStroke(new BasicStroke(5));
                g2d.drawLine(centerX, centerY, centerX, height - 100);

                hue += 0.005f;
                if (hue > 1.0f) {
                    hue = 0.0f;
                }
                Color petalColor = Color.getHSBColor(hue, 0.8f, 0.9f);
                g2d.setColor(petalColor);

                int numPetals = 6;
                int petalRadius = 45;
                for (int i = 0; i < numPetals; i++) {
                    double angle = Math.toRadians(i * (360.0 / numPetals));
                    int x = (int) (centerX + Math.cos(angle) * petalRadius);
                    int y = (int) (centerY + Math.sin(angle) * petalRadius);
                    g2d.fillOval(x - 30, y - 30, 60, 60);
                }

                g2d.setColor(Color.ORANGE);
                g2d.fillOval(centerX - 25, centerY - 25, 50, 50);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
                String text = "You found the Graphicflower!";
                int textWidth = g2d.getFontMetrics().stringWidth(text);
                g2d.drawString(text, (width - textWidth) / 2, height - 50);
            }
        };

        frame.add(panel);
        frame.setVisible(true);

        Timer timer = new Timer(16, e -> panel.repaint());
        timer.start();
    }

    static class DecompilerClassVisitor extends ClassVisitor {

        private String className;
        private final PrintWriter writer;

        public DecompilerClassVisitor(PrintWriter writer) {
            super(Opcodes.ASM9);
            this.writer = writer;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name.substring(name.lastIndexOf('/') + 1);
            writer.println("public class " + className + " {");
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            if (name.equals("<init>")) {
                return null;
            }
            writer.print("\n    public static void " + name + "() {");
            return new DecompilerMethodVisitor(writer);
        }

        @Override
        public void visitEnd() {
            writer.println("\n}");
        }
    }

    static class DecompilerMethodVisitor extends MethodVisitor {

        private final Stack<Expression> astStack = new Stack<>();
        private final PrintWriter writer;

        public DecompilerMethodVisitor(PrintWriter writer) {
            super(Opcodes.ASM9);
            this.writer = writer;
        }

        @Override
        public void visitInsn(int opcode) {
            switch (opcode) {
                case Opcodes.IADD ->
                    handleBinaryOperation("+");
                case Opcodes.ISUB ->
                    handleBinaryOperation("-");
                case Opcodes.IMUL ->
                    handleBinaryOperation("*");
                case Opcodes.IDIV ->
                    handleBinaryOperation("/");
                case Opcodes.RETURN -> {
                }
            }
        }

        private void handleBinaryOperation(String operator) {
            if (astStack.size() >= 2) {
                Expression right = astStack.pop();
                Expression left = astStack.pop();
                astStack.push(new BinaryExpr(left, operator, right));
            }
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
                astStack.push(new LiteralExpr(operand));
            }
        }

        @Override
        public void visitLdcInsn(Object value) {
            astStack.push(new LiteralExpr(value));
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if (opcode == Opcodes.GETSTATIC && name.equals("out")) {
                astStack.push(new VarExpr("System.out"));
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (name.equals("println") && !astStack.isEmpty()) {
                Expression argument = astStack.pop();
                Expression target = !astStack.isEmpty() ? astStack.pop() : new VarExpr("System.out");
                writer.print("\n        " + target.print() + "." + name + "(" + argument.print() + ");");
            }
        }

        @Override
        public void visitVarInsn(int opcode, int varIndex) {
            if (opcode == Opcodes.ILOAD || opcode == Opcodes.ALOAD) {
                astStack.push(new VarExpr("var" + varIndex));
            }
        }

        @Override
        public void visitEnd() {
            writer.print("\n    }");
        }
    }
}
