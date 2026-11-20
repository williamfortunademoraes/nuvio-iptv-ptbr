import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31c;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class DexTool {
    static final String[] UI_PACKAGES = {
            "Lcom/nuvio/tv/livetv/ui/",
            "Lcom/nuvio/tv/livetv/sports/",
            "Lcom/nuvio/tv/livetv/record/",
            "Lcom/nuvio/tv/livetv/multiview/",
            "Lcom/nuvio/tv/livetv/calendar/",
            "Lcom/nuvio/tv/livetv/ondemand/",
            "Lcom/nuvio/tv/livetv/home/",
            "Lcom/nuvio/tv/livetv/player/",
    };
    static final String[] UI_BANNED = {
            "Repository", "ViewModel", "SideMenuPages", "Service", "Recorder",
            "Feeds", "Listings", "Catalog", "Db", "Store", "Prefs", "Worker",
            "Sync", "Model$"
    };

    static boolean allowWide(String s) {
        // Long, unambiguous user-facing sentences can be patched in any class (obfuscated
        // Compose classes included), as long as they are not keys/identifiers.
        if (s.length() < 10 || s.indexOf(' ') < 0) return false;
        if (s.contains("$") || s.contains("_")) return false;
        int letters = 0, other = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) letters++; else other++;
        }
        return letters * 2 > s.length() && other > 0;
    }

    static Set<String> EXTRA_UI = new HashSet<>();

    static boolean isUiClass(String type) {
        String simpleName = type.substring(type.lastIndexOf('/') + 1);
        if (simpleName.startsWith("SideMenuPages")) return false;
        if (EXTRA_UI.contains(type)) return true;
        boolean ok = false;
        for (String p : UI_PACKAGES) {
            if (type.startsWith(p)) { ok = true; break; }
        }
        if (!ok) return type.equals("Lcom/nuvio/tv/MainActivity;");
        for (String b : UI_BANNED) {
            if (simpleName.contains(b)) return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        File apk = new File(args[1]);
        MultiDexContainer<? extends DexFile> container = DexFileFactory.loadDexContainer(apk, Opcodes.getDefault());
        if (mode.equals("report")) {
            Map<String, Set<String>> map = new TreeMap<>();
            for (String name : container.getDexEntryNames()) {
                MultiDexContainer.DexEntry<? extends DexFile> entry = container.getEntry(name);
                if (entry == null) continue;
                for (ClassDef cd : entry.getDexFile().getClasses()) {
                    for (Method m : cd.getMethods()) {
                        if (m.getImplementation() == null) continue;
                        for (Instruction ins : m.getImplementation().getInstructions()) {
                            if (!(ins instanceof ReferenceInstruction)) continue;
                            Reference r = ((ReferenceInstruction) ins).getReference();
                            if (!(r instanceof StringReference)) continue;
                            String s = ((StringReference) r).getString();
                            if (s.length() < 2) continue;
                            map.computeIfAbsent(s, k -> new TreeSet<>()).add(cd.getType() + " :: " + m.getName());
                        }
                    }
                }
            }
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(args[2]), StandardCharsets.UTF_8))) {
                for (Map.Entry<String, Set<String>> e : map.entrySet()) {
                    out.println(e.getKey().replace("\n", "\\n").replace("\t", "\\t") + "\t" + e.getValue().size()
                            + "\t" + (e.getValue().stream().anyMatch(v -> isUiClass(v.substring(0, v.indexOf(" ")))) ? "UI" : "-")
                            + "\t" + String.join(" | ", e.getValue()).replace("\t", " "));
                }
            }
            System.out.println("strings: " + map.size());
            return;
        }
        if (mode.equals("patch")) {
            Map<String, String> mapping = new LinkedHashMap<>();
            for (String line : Files.readAllLines(Paths.get(args[2]), StandardCharsets.UTF_8)) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] parts = line.split("\t", 2);
                if (parts.length == 2) mapping.put(parts[0], parts[1]);
            }
            if (args.length > 4) {
                for (String line : Files.readAllLines(Paths.get(args[4]), StandardCharsets.UTF_8)) {
                    if (!line.isBlank()) EXTRA_UI.add(line.trim());
                }
            }
            File outDir = new File(args[3]);
            outDir.mkdirs();
            int replaced = 0, files = 0;
            Set<String> used = new HashSet<>();
            for (String name : container.getDexEntryNames()) {
                MultiDexContainer.DexEntry<? extends DexFile> entry = container.getEntry(name);
                if (entry == null) continue;
                DexFile dex = entry.getDexFile();
                List<ClassDef> newClasses = new ArrayList<>();
                boolean changedDex = false;
                for (ClassDef cd : dex.getClasses()) {
                    boolean isUi = isUiClass(cd.getType());
                    List<Method> newMethods = new ArrayList<>();
                    boolean changedClass = false;
                    for (Method m : cd.getMethods()) {
                        if (m.getImplementation() == null || (m.getName().equals("<clinit>") && !isUi)) { newMethods.add(m); continue; }
                        MutableMethodImplementation impl = new MutableMethodImplementation(m.getImplementation());
                        List<BuilderInstruction> insns = impl.getInstructions();
                        boolean changedMethod = false;
                        for (int i = 0; i < insns.size(); i++) {
                            BuilderInstruction bi = insns.get(i);
                            if (!(bi instanceof BuilderInstruction21c) && !(bi instanceof BuilderInstruction31c)) continue;
                            Reference ref;
                            int reg;
                            if (bi instanceof BuilderInstruction21c) {
                                ref = ((BuilderInstruction21c) bi).getReference();
                                reg = ((BuilderInstruction21c) bi).getRegisterA();
                            } else {
                                ref = ((BuilderInstruction31c) bi).getReference();
                                reg = ((BuilderInstruction31c) bi).getRegisterA();
                            }
                            if (!(ref instanceof StringReference)) continue;
                            String s = ((StringReference) ref).getString();
                            String pt = mapping.get(s);
                            if (pt == null) continue;
                            if (!isUi && !allowWide(s)) continue;
                            BuilderInstruction replacement;
                            if (bi.getOpcode() == Opcode.CONST_STRING) {
                                replacement = new BuilderInstruction21c(Opcode.CONST_STRING, reg, new ImmutableStringReference(pt));
                            } else if (bi.getOpcode() == Opcode.CONST_STRING_JUMBO) {
                                replacement = new BuilderInstruction31c(Opcode.CONST_STRING_JUMBO, reg, new ImmutableStringReference(pt));
                            } else {
                                continue;
                            }
                            impl.replaceInstruction(i, replacement);
                            changedMethod = true;
                            replaced++;
                            used.add(s);
                        }
                        if (changedMethod) {
                            newMethods.add(new ImmutableMethod(m.getDefiningClass(), m.getName(), m.getParameters(),
                                    m.getReturnType(), m.getAccessFlags(), m.getAnnotations(), m.getHiddenApiRestrictions(), impl));
                            changedClass = true;
                        } else {
                            newMethods.add(m);
                        }
                    }
                    if (changedClass) {
                        newClasses.add(new ImmutableClassDef(cd.getType(), cd.getAccessFlags(), cd.getSuperclass(),
                                cd.getInterfaces(), cd.getSourceFile(), cd.getAnnotations(), cd.getFields(), newMethods));
                        changedDex = true;
                    } else {
                        newClasses.add(cd);
                    }
                }
                DexFile newDex = changedDex
                        ? new ImmutableDexFile(dex.getOpcodes(), newClasses)
                        : dex;
                DexPool.writeTo(new File(outDir, name).getAbsolutePath(), newDex);
                files++;
            }
            List<String> missing = new ArrayList<>();
            for (String k : mapping.keySet()) if (!used.contains(k)) missing.add(k);
            System.out.println("dex files: " + files + ", replacements: " + replaced + ", used keys: " + used.size() + "/" + mapping.size());
            if (!missing.isEmpty()) {
                System.out.println("not found (" + missing.size() + "):");
                for (String m : missing) System.out.println("  " + m);
            }
            return;
        }
        throw new IllegalArgumentException("mode?");
    }
}
