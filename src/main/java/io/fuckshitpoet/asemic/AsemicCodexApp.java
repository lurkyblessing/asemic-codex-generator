package io.fuckshitpoet.asemic;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

/** A self-contained Java2D artifact press for invented writing systems. */
public final class AsemicCodexApp {
    private static final Color INK = new Color(37, 43, 43);
    private static final Color WINE = new Color(104, 35, 35);
    private static final Color GOLD = new Color(177, 133, 43);

    private final JTextArea poem = new JTextArea("In the archive of sleep\nI found a language made of moth wings.\nEach word opened once\nand would not translate.");
    private final JTextField seed = new JTextField("seraph-1313");
    private final JTextArea translation = new JTextArea();
    private final CodexPanel page = new CodexPanel();
    private Script script;

    private AsemicCodexApp() {
        JFrame frame = new JFrame("Asemic Codex Generator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1080, 720));
        frame.setLayout(new BorderLayout());
        frame.add(toolbar(), BorderLayout.NORTH);

        poem.setFont(new Font(Font.SERIF, Font.PLAIN, 16));
        poem.setLineWrap(true); poem.setWrapStyleWord(true);
        translation.setEditable(false); translation.setLineWrap(true); translation.setWrapStyleWord(true);
        translation.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        translation.setForeground(new Color(48, 69, 68));
        JPanel editor = new JPanel(new GridLayout(2, 1, 0, 10));
        editor.setBackground(new Color(29, 35, 35));
        editor.setBorder(new EmptyBorder(14, 14, 14, 14));
        editor.add(labeled("YOUR POEM", new JScrollPane(poem)));
        editor.add(labeled("THE UNTRANSLATABLE READING", new JScrollPane(translation)));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor, page);
        split.setResizeWeight(.36); split.setDividerLocation(420);
        frame.add(split, BorderLayout.CENTER);
        transmute();
        frame.pack(); frame.setLocationByPlatform(true); frame.setVisible(true);
    }

    private JComponent toolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 9));
        bar.setBackground(new Color(24, 29, 30));
        JLabel title = new JLabel("ASEMIC CODEX  /  THE ARTIFACT PRESS");
        title.setForeground(new Color(222, 190, 115)); title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JLabel seedLabel = new JLabel("SEED"); seedLabel.setForeground(Color.LIGHT_GRAY);
        seed.setColumns(15);
        JButton make = button("TRANSMUTE CODEX", e -> transmute());
        JButton export = button("EXPORT PNG", e -> export());
        bar.add(title); bar.add(Box.createHorizontalStrut(18)); bar.add(seedLabel); bar.add(seed); bar.add(make); bar.add(export);
        return bar;
    }
    private static JButton button(String text, java.awt.event.ActionListener action) {
        JButton b = new JButton(text); b.addActionListener(action); b.setFocusPainted(false); return b;
    }
    private static JComponent labeled(String label, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 6)); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setForeground(new Color(222, 190, 115)); l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        p.add(l, BorderLayout.NORTH); p.add(body, BorderLayout.CENTER); return p;
    }
    private void transmute() {
        script = new Script(seed.getText().trim().isBlank() ? "unnamed" : seed.getText().trim());
        String source = poem.getText();
        translation.setText(script.transliterate(source));
        page.configure(script, source);
        page.repaint();
    }
    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("asemic-codex.png"));
        if (chooser.showSaveDialog(page) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".png")) file = new File(file + ".png");
        BufferedImage image = page.render(1600, 2100);
        try { ImageIO.write(image, "png", file); }
        catch (IOException ex) { JOptionPane.showMessageDialog(page, "Could not write the codex: " + ex.getMessage(), "Export failed", JOptionPane.ERROR_MESSAGE); }
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(AsemicCodexApp::new); }

    static final class Script {
        private final long key;
        private final Map<Character, Glyph> alphabet = new HashMap<>();
        private final Map<String, String> ligatures = Map.of("th", "ϟ", "sh", "Ϟ", "ch", "Ϡ", "oo", "꙰", "ll", "ꜥ", "ea", "ꝏ");
        Script(String seed) {
            key = seed.hashCode() * 1103515245L + 12345;
            Random r = new Random(key);
            for (char c = 'a'; c <= 'z'; c++) alphabet.put(c, new Glyph(r.nextLong()));
        }
        String transliterate(String text) {
            StringBuilder out = new StringBuilder(); String lower = text.toLowerCase(Locale.ROOT);
            for (int i = 0; i < lower.length(); i++) {
                String pair = i + 1 < lower.length() ? lower.substring(i, i + 2) : "";
                if (ligatures.containsKey(pair)) { out.append(ligatures.get(pair)); i++; }
                else { char c = lower.charAt(i); out.append(c >= 'a' && c <= 'z' ? glyphChar(c) : punct(c)); }
            }
            return out.toString();
        }
        private String glyphChar(char c) { return String.valueOf((char) (0xE000 + (c - 'a'))); }
        private String punct(char c) { return switch (c) { case '.' -> " ✣"; case ',' -> " ᛫"; case '?' -> " 〄"; case '!' -> " ❦"; case '\n' -> "\n"; default -> " "; }; }
        Glyph glyph(char c) { return alphabet.get(c); }
        String grammar() { return "Grammar of the " + Long.toUnsignedString(key, 36).toUpperCase(Locale.ROOT) + " hand:  •  verbs end in a hooked ascender  •  breath marks reverse the next word  •  th / sh / ch bind as sacred ligatures"; }
    }

    static final class Glyph {
        final long seed;
        Glyph(long seed) { this.seed = seed; }
        void paint(Graphics2D g, float x, float baseline, float scale) {
            Random r = new Random(seed); g.setStroke(new BasicStroke(2.2f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float w = 20 * scale, h = 29 * scale;
            for (int i = 0, n = 2 + r.nextInt(3); i < n; i++) {
                float x1 = x + (r.nextFloat() * w), y1 = baseline - r.nextFloat() * h;
                float cx = x + r.nextFloat() * w, cy = baseline - r.nextFloat() * h;
                float x2 = x + r.nextFloat() * w, y2 = baseline - r.nextFloat() * h;
                g.draw(new QuadCurve2D.Float(x1, y1, cx, cy, x2, y2));
            }
            if (r.nextBoolean()) g.draw(new Ellipse2D.Float(x + r.nextFloat()*w*.6f, baseline-h*(.35f+r.nextFloat()*.4f), 4*scale, 4*scale));
            if (r.nextBoolean()) g.fill(new Ellipse2D.Float(x + r.nextFloat()*w, baseline-h*(.7f+r.nextFloat()*.25f), 2.3f*scale, 2.3f*scale));
        }
    }

    final class CodexPanel extends JPanel {
        private Script current; private String source = "";
        CodexPanel() { setBackground(new Color(33, 38, 38)); }
        void configure(Script script, String poem) { current = script; source = poem; }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); if (current != null) draw((Graphics2D) g, getWidth(), getHeight()); }
        BufferedImage render(int w, int h) { BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); Graphics2D g = img.createGraphics(); draw(g, w, h); g.dispose(); return img; }
        private void draw(Graphics2D g, int w, int h) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Random r = new Random(current.key ^ 0x5c0d3L);
            g.setColor(new Color(49, 52, 49)); g.fillRect(0, 0, w, h);
            int m = Math.max(22, w / 18); Rectangle pageRect = new Rectangle(m, m/2, w-2*m, h-m);
            g.setColor(new Color(223, 207, 159)); g.fillRoundRect(pageRect.x, pageRect.y, pageRect.width, pageRect.height, 8, 8);
            for (int i=0;i<2400;i++) { int a=3+r.nextInt(10); g.setColor(new Color(108, 76, 37, a)); int x=pageRect.x+r.nextInt(pageRect.width), y=pageRect.y+r.nextInt(pageRect.height); g.fillRect(x,y,1+r.nextInt(3),1+r.nextInt(3)); }
            g.setColor(new Color(119, 72, 38)); g.setStroke(new BasicStroke(Math.max(1, w/500f))); g.drawRect(pageRect.x+10,pageRect.y+10,pageRect.width-20,pageRect.height-20);
            float s = w / 900f; int left = pageRect.x + (int)(85*s), right = pageRect.x + pageRect.width - (int)(75*s), top = pageRect.y + (int)(160*s);
            g.setColor(WINE); g.setFont(new Font(Font.SERIF, Font.BOLD, (int)(28*s))); g.drawString("THE  " + Long.toUnsignedString(current.key, 36).toUpperCase(Locale.ROOT) + "  FRAGMENT", left, pageRect.y+(int)(68*s));
            g.setColor(GOLD); g.setStroke(new BasicStroke(2*s)); g.drawLine(left, pageRect.y+(int)(82*s), right, pageRect.y+(int)(82*s));
            List<String> lines = Arrays.asList(source.split("\\R")); int lineNo=0;
            for (String line : lines) { paintLine(g, line, left, top + lineNo*(int)(52*s), right, s, lineNo==0); lineNo++; }
            // marginal botanical knotwork
            g.setColor(new Color(104, 35, 35, 190)); g.setStroke(new BasicStroke(1.5f*s));
            for(int y=top; y<pageRect.y+pageRect.height-90*s; y+=(int)(73*s)) { float x=pageRect.x+38*s; g.draw(new Arc2D.Float(x,y,28*s,28*s,0,260,Arc2D.OPEN)); g.fill(new Ellipse2D.Float(x+10*s,y+11*s,4*s,4*s)); }
            g.setColor(INK); g.setFont(new Font(Font.SERIF, Font.ITALIC, (int)(13*s))); g.drawString(current.grammar(), left, pageRect.y+pageRect.height-(int)(38*s));
        }
        private void paintLine(Graphics2D g, String line, int left, int y, int right, float scale, boolean initial) {
            float x=left; String lower=line.toLowerCase(Locale.ROOT); boolean first=true;
            for (int i=0;i<lower.length() && x<right-25*scale;i++) {
                char c=lower.charAt(i);
                if (Character.isLetter(c)) { if (first && initial) { g.setColor(GOLD); g.fillRoundRect((int)(x-5*scale),(int)(y-38*scale),(int)(36*scale),(int)(45*scale),4,4); g.setColor(WINE); } else g.setColor(INK); current.glyph(c).paint(g,x,y,scale*(first&&initial?1.25f:1)); x+=26*scale; first=false; }
                else if (c==' ') { x+=13*scale; first=false; }
                else { g.setColor(WINE); g.fill(new Ellipse2D.Float(x,y-10*scale,4*scale,4*scale)); x+=12*scale; }
            }
        }
    }
}
