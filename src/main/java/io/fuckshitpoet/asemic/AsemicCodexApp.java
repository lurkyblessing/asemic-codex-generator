package io.fuckshitpoet.asemic;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A self-contained Java2D artifact press for invented writing systems. */
public final class AsemicCodexApp {
    private static final Color[] PALETTE = {
        new Color(155, 20, 30),  // Deep red
        new Color(20, 30, 180),  // Cobalt Blue
        new Color(30, 120, 50),  // Forest Green
        new Color(200, 130, 50), // Warm Ochre
        new Color(200, 20, 100), // Magenta
        new Color(50, 140, 150), // Teal
        new Color(110, 30, 150)  // Purple
    };

    private final JTextArea poem = new JTextArea("boy crazed\nslow haze\ndon’t blame me for trying to pass my days\n\n-fuckshitpoem");
    private final JTextField seed = new JTextField("seraph-1313");
    private final JTextPane translation = new JTextPane();
    private final CodexPanel page = new CodexPanel();
    private Script script;

    private AsemicCodexApp() {
        JFrame frame = new JFrame("Asemic Codex Generator");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1080, 720));
        frame.setLayout(new BorderLayout());
        frame.add(toolbar(), BorderLayout.NORTH);

        poem.setFont(new Font(Font.SERIF, Font.PLAIN, 18));
        poem.setLineWrap(true); poem.setWrapStyleWord(true);
        poem.setBackground(new Color(24, 29, 30));
        poem.setForeground(new Color(220, 220, 220));
        poem.setCaretColor(Color.WHITE);

        translation.setEditable(false);
        translation.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        translation.setBackground(new Color(24, 29, 30));

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
        bar.setBackground(new Color(18, 22, 23));
        JLabel title = new JLabel("ASEMIC CODEX  /  THE ARTIFACT PRESS");
        title.setForeground(new Color(222, 190, 115)); title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JLabel seedLabel = new JLabel("SEED"); seedLabel.setForeground(Color.LIGHT_GRAY);
        seed.setColumns(15);
        seed.setBackground(new Color(40, 45, 45)); seed.setForeground(Color.WHITE);
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
        p.add(l, BorderLayout.NORTH);
        
        JScrollPane scroll = (JScrollPane) body;
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 60)));
        scroll.getViewport().setBackground(new Color(24, 29, 30));
        p.add(body, BorderLayout.CENTER); return p;
    }

    private void transmute() {
        script = new Script(seed.getText().trim().isBlank() ? "unnamed" : seed.getText().trim());
        String source = poem.getText();
        
        translation.setText("");
        StyledDocument doc = translation.getStyledDocument();
        Matcher m = Pattern.compile("([a-zA-Z']+)|([^a-zA-Z']+)").matcher(source);
        try {
            while (m.find()) {
                if (m.group(1) != null) {
                    String word = m.group(1);
                    WordGlyph wg = script.wordGlyph(word);
                    Style style = translation.addStyle("color", null);
                    StyleConstants.setForeground(style, wg.color);
                    doc.insertString(doc.getLength(), word, style);
                }
                if (m.group(2) != null) {
                    Style style = translation.addStyle("plain", null);
                    StyleConstants.setForeground(style, new Color(130, 140, 140));
                    doc.insertString(doc.getLength(), m.group(2), style);
                }
            }
        } catch (Exception e) {}

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
        final long key;
        private final Map<String, WordGlyph> cache = new HashMap<>();
        
        Script(String seed) {
            key = seed.hashCode() * 1103515245L + 12345;
        }
        
        WordGlyph wordGlyph(String word) {
            String lower = word.toLowerCase(Locale.ROOT);
            return cache.computeIfAbsent(lower, w -> new WordGlyph(w, key));
        }
        
        String grammar() { return "Codex rules: Colors distinguish root concepts. Calligraphic weight indicates stress."; }
    }

    static final class WordGlyph {
        final long seed;
        final Color color;
        
        WordGlyph(String word, long scriptKey) {
            this.seed = (word.hashCode() * 31L) ^ scriptKey;
            this.color = PALETTE[Math.abs((int) (seed % PALETTE.length))];
        }
        
        void paint(Graphics2D g, float x, float y, float w, float h) {
            Random r = new Random(seed);
            g.setColor(color);
            float penAngle = -(float)Math.PI/8f + r.nextFloat() * (float)Math.PI/4f;
            int numComponents = 3 + r.nextInt(3);
            
            float cx = x + w/2f;
            float cy = y - h/2f;
            
            for (int i = 0; i < numComponents; i++) {
                float startX = cx + (r.nextFloat()-0.5f)*w*0.8f;
                float startY = cy + (r.nextFloat()-0.5f)*h*0.8f;
                float cp1x = cx + (r.nextFloat()-0.5f)*w*1.0f;
                float cp1y = cy + (r.nextFloat()-0.5f)*h*1.0f;
                float cp2x = cx + (r.nextFloat()-0.5f)*w*1.0f;
                float cp2y = cy + (r.nextFloat()-0.5f)*h*1.0f;
                float endX = cx + (r.nextFloat()-0.5f)*w*0.8f;
                float endY = cy + (r.nextFloat()-0.5f)*h*0.8f;
                
                float maxThick = w * 0.09f + r.nextFloat() * w * 0.04f;
                int pressureType = r.nextInt(4);
                
                drawCursiveBrush(g, startX, startY, cp1x, cp1y, cp2x, cp2y, endX, endY, maxThick, penAngle, pressureType);
            }
            
            int numDeco = 1 + r.nextInt(2);
            for (int i = 0; i < numDeco; i++) {
                float type = r.nextFloat();
                float dx = x + w/2f + (r.nextFloat()-0.5f)*w*0.8f;
                float dy = y - h/2f + (r.nextFloat()-0.5f)*h*0.8f;
                float decoThick = w * 0.08f + r.nextFloat() * w * 0.05f;
                
                if (type < 0.3f) {
                    g.fill(new Ellipse2D.Float(dx - decoThick/2f, dy - decoThick/2f, decoThick, decoThick));
                } else if (type < 0.6f) {
                    float length = w * 0.4f + r.nextFloat() * w * 0.3f;
                    float angle = r.nextBoolean() ? 0 : (float)Math.PI/4f;
                    float sx = dx - (float)Math.cos(angle)*length/2f;
                    float sy = dy - (float)Math.sin(angle)*length/2f;
                    float ex = dx + (float)Math.cos(angle)*length/2f;
                    float ey = dy + (float)Math.sin(angle)*length/2f;
                    drawCursiveBrush(g, sx, sy, dx, dy, dx, dy, ex, ey, decoThick, penAngle, 1);
                } else {
                    drawCursiveBrush(g, dx, dy, dx + w * 0.3f, dy - h * 0.3f, dx - w * 0.3f, dy + h * 0.3f, dx + w * 0.1f, dy + h * 0.1f, decoThick, penAngle, 0);
                }
            }
        }
        
        private void drawCursiveBrush(Graphics2D g, float x1, float y1, float cp1x, float cp1y, float cp2x, float cp2y, float x2, float y2, float maxThick, float penAngle, int pressureType) {
            int steps = 250;
            float penWidth = maxThick;
            float penHeight = maxThick * 0.15f;
            
            AffineTransform orig = g.getTransform();
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                float inv = 1 - t;
                
                float px = inv*inv*inv*x1 + 3*inv*inv*t*cp1x + 3*inv*t*t*cp2x + t*t*t*x2;
                float py = inv*inv*inv*y1 + 3*inv*inv*t*cp1y + 3*inv*t*t*cp2y + t*t*t*y2;
                
                float pressure = 1.0f;
                if (pressureType == 0) pressure = (float)Math.sin(t * Math.PI);
                else if (pressureType == 1) pressure = (float)Math.pow(1 - t, 0.5);
                else if (pressureType == 2) pressure = (float)Math.pow(t, 0.5);
                else pressure = 0.8f + 0.2f*(float)Math.sin(t*Math.PI*3);
                
                float w = penWidth * Math.max(0.1f, pressure);
                float h = penHeight * Math.max(0.1f, pressure);
                
                g.translate(px, py);
                g.rotate(penAngle);
                g.fill(new Ellipse2D.Float(-w/2, -h/2, w, h));
                g.setTransform(orig);
            }
        }
    }

    final class CodexPanel extends JPanel {
        private Script current; private String source = "";
        CodexPanel() { setBackground(new Color(29, 35, 35)); }
        void configure(Script script, String poem) { current = script; source = poem; }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); if (current != null) draw((Graphics2D) g, getWidth(), getHeight()); }
        BufferedImage render(int w, int h) { BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); Graphics2D g = img.createGraphics(); draw(g, w, h); g.dispose(); return img; }
        
        private void draw(Graphics2D g, int w, int h) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Random r = new Random(current.key ^ 0x5c0d3L);
            g.setColor(new Color(30, 25, 22)); g.fillRect(0, 0, w, h); // Desk background
            
            int m = Math.max(22, w / 18); Rectangle pageRect = new Rectangle(m, m/2, w-2*m, h-m);
            // Warm beige/papyrus background
            g.setColor(new Color(234, 222, 203)); 
            g.fillRoundRect(pageRect.x, pageRect.y, pageRect.width, pageRect.height, 12, 12);
            
            // Subtle papyrus grain
            for (int i = 0; i < 4000; i++) {
                int a = 3 + r.nextInt(6);
                g.setColor(new Color(160, 140, 120, a));
                int px = pageRect.x + r.nextInt(pageRect.width);
                int py = pageRect.y + r.nextInt(pageRect.height);
                g.fillRect(px, py, 1 + r.nextInt(2), 1 + r.nextInt(2));
            }
            
            float s = w / 900f; 
            
            // Decorative Egyptian-style floral border
            int bw = (int)(50 * s); // border width
            g.setColor(new Color(215, 205, 185)); // slightly darker margin base
            g.fillRoundRect(pageRect.x + 8, pageRect.y + 8, pageRect.width - 16, pageRect.height - 16, 8, 8);
            g.setColor(new Color(234, 222, 203)); // inner paper
            g.fillRoundRect(pageRect.x + bw, pageRect.y + bw, pageRect.width - 2*bw, pageRect.height - 2*bw, 12, 12);
            
            // Border motifs
            for (int bx = pageRect.x + bw + (int)(30*s); bx < pageRect.x + pageRect.width - bw - (int)(30*s); bx += (int)(60*s)) {
                g.setColor(new Color(100, 140, 150)); 
                g.fillArc(bx - (int)(15*s), pageRect.y + (int)(15*s), (int)(30*s), (int)(25*s), 180, 180);
                g.fillArc(bx - (int)(15*s), pageRect.y + pageRect.height - (int)(40*s), (int)(30*s), (int)(25*s), 0, 180);
                
                g.setColor(new Color(175, 55, 55)); 
                g.fillPolygon(new int[]{bx, bx-(int)(20*s), bx-(int)(10*s)}, new int[]{pageRect.y+(int)(15*s), pageRect.y+(int)(25*s), pageRect.y+(int)(35*s)}, 3);
                g.fillPolygon(new int[]{bx, bx+(int)(20*s), bx+(int)(10*s)}, new int[]{pageRect.y+(int)(15*s), pageRect.y+(int)(25*s), pageRect.y+(int)(35*s)}, 3);
                g.fillPolygon(new int[]{bx, bx-(int)(20*s), bx-(int)(10*s)}, new int[]{pageRect.y+pageRect.height-(int)(15*s), pageRect.y+pageRect.height-(int)(25*s), pageRect.y+pageRect.height-(int)(35*s)}, 3);
                g.fillPolygon(new int[]{bx, bx+(int)(20*s), bx+(int)(10*s)}, new int[]{pageRect.y+pageRect.height-(int)(15*s), pageRect.y+pageRect.height-(int)(25*s), pageRect.y+pageRect.height-(int)(35*s)}, 3);
            }
            for (int by = pageRect.y + bw + (int)(30*s); by < pageRect.y + pageRect.height - bw - (int)(30*s); by += (int)(60*s)) {
                g.setColor(new Color(100, 140, 150)); 
                g.fillArc(pageRect.x + (int)(15*s), by - (int)(15*s), (int)(25*s), (int)(30*s), -90, 180);
                g.fillArc(pageRect.x + pageRect.width - (int)(40*s), by - (int)(15*s), (int)(25*s), (int)(30*s), 90, 180);
                
                g.setColor(new Color(175, 55, 55)); 
                g.fillPolygon(new int[]{pageRect.x+(int)(15*s), pageRect.x+(int)(25*s), pageRect.x+(int)(35*s)}, new int[]{by, by-(int)(20*s), by-(int)(10*s)}, 3);
                g.fillPolygon(new int[]{pageRect.x+(int)(15*s), pageRect.x+(int)(25*s), pageRect.x+(int)(35*s)}, new int[]{by, by+(int)(20*s), by+(int)(10*s)}, 3);
                g.fillPolygon(new int[]{pageRect.x+pageRect.width-(int)(15*s), pageRect.x+pageRect.width-(int)(25*s), pageRect.x+pageRect.width-(int)(35*s)}, new int[]{by, by-(int)(20*s), by-(int)(10*s)}, 3);
                g.fillPolygon(new int[]{pageRect.x+pageRect.width-(int)(15*s), pageRect.x+pageRect.width-(int)(25*s), pageRect.x+pageRect.width-(int)(35*s)}, new int[]{by, by+(int)(20*s), by+(int)(10*s)}, 3);
            }
            
            // Inner thin border
            int innerM = bw + (int)(8*s);
            g.setColor(new Color(150, 120, 90)); 
            g.setStroke(new BasicStroke(2.5f*s));
            g.drawRoundRect(pageRect.x + innerM, pageRect.y + innerM, pageRect.width - 2*innerM, pageRect.height - 2*innerM, 20, 20);
            
            // Writing area layout (centered within inner border)
            int left = pageRect.x + innerM + (int)(80*s);
            int right = pageRect.x + pageRect.width - innerM - (int)(80*s);
            int top = pageRect.y + innerM + (int)(200*s);
            int bottom = pageRect.y + pageRect.height - innerM - (int)(100*s);
            String headerText = "THE  " + Long.toUnsignedString(current.key, 36).toUpperCase(Locale.ROOT) + "  FRAGMENT";
            g.setColor(new Color(155, 40, 40)); 
            g.setFont(new Font(Font.SERIF, Font.BOLD, (int)(22*s))); 
            FontMetrics fm = g.getFontMetrics();
            int headerX = pageRect.x + (pageRect.width - fm.stringWidth(headerText)) / 2;
            g.drawString(headerText, headerX, pageRect.y + innerM + (int)(45*s));
            
            // Lines
            List<String> lines = Arrays.asList(source.split("\\R")); 
            int lineNo = 0;
            for (String line : lines) { 
                float lineWidth = paintLine(g, line, left, top + lineNo * (int)(110*s), right, s, lineNo == 0, true);
                float offset = Math.max(0, (right - left - lineWidth) / 2);
                paintLine(g, line, left + (int)offset, top + lineNo * (int)(110*s), right, s, lineNo == 0, false); 
                lineNo++; 
            }
            
            // Footer text
            String footer = current.grammar();
            g.setColor(new Color(120, 100, 80)); 
            g.setFont(new Font(Font.SERIF, Font.ITALIC, (int)(14*s))); 
            fm = g.getFontMetrics();
            int footerX = pageRect.x + (pageRect.width - fm.stringWidth(footer)) / 2;
            g.drawString(footer, footerX, pageRect.y + pageRect.height - innerM - (int)(25*s));
        }
        
        private float paintLine(Graphics2D g, String line, int left, int y, int right, float scale, boolean initial, boolean measureOnly) {
            float x = measureOnly ? 0 : left;
            float startX = x;
            Matcher m = Pattern.compile("([a-zA-Z']+)|([^a-zA-Z']+)").matcher(line);
            boolean firstWord = true;
            
            while (m.find()) {
                if (m.group(1) != null) {
                    String word = m.group(1);
                    WordGlyph wg = current.wordGlyph(word);
                    float gw = 50 * scale + Math.min(4, word.length()) * 12 * scale;
                    float gh = 85 * scale;
                    
                    if (x + gw > (measureOnly ? right - left : right)) break;
                    
                    if (!measureOnly) {
                        wg.paint(g, x, y, gw, gh);
                    }
                    x += gw + 12 * scale;
                    firstWord = false;
                }
                if (m.group(2) != null) {
                    for (char c : m.group(2).toCharArray()) {
                        if (c == ' ' || c == '\t') x += 20 * scale;
                        else if (c == '\n') {} 
                        else {
                            if (!measureOnly) {
                                g.setColor(new Color(180, 50, 50, 180));
                                g.fill(new Ellipse2D.Float(x, y - 10 * scale, 6 * scale, 6 * scale));
                            }
                            x += 15 * scale;
                        }
                    }
                }
            }
            return x - startX;
        }
    }
}
