package com.terraforged.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class NBTLeavesFixer {

    public static void main(String[] args) {
        RuleSet ruleSet = new RuleSet();
        ruleSet.put("persistent", new Rule("true", "false"));
        String path = "H:\\Projects\\TerraForged\\TerraForgedMod\\src\\main\\resources\\data\\terraforged\\structures\\trees";
        visit(new File(path), ruleSet);
    }

    public static void visit(File file, RuleSet ruleSet) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                visit(f, ruleSet);
            }
        } else if (file.getName().endsWith(".nbt")) {
            try {
                System.out.println("Opening file: " + file);
                CompoundNBT value;
                INBT result;
                try (InputStream in = new FileInputStream(file)) {
                    value = CompressedStreamTools.readCompressed(in);
                    result = modify("", value, ruleSet);
                    if (value == result) {
                        return;
                    }
                }
                System.out.println("Writing file: " + file);
                try (OutputStream out = new FileOutputStream(file)) {
                    CompressedStreamTools.writeCompressed((CompoundNBT) result, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static INBT modify(String name, INBT nbt, RuleSet ruleSet) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT map = (CompoundNBT) nbt;
            boolean change = false;
            for (String key : map.keySet()) {
                INBT value = map.get(key);
                INBT result = modify(key, value, ruleSet);
                map.put(key, result);
                change |= value != result;
            }
            return change ? map.copy() : map;
        } else if (nbt instanceof ListNBT) {
            ListNBT list = (ListNBT) nbt;
            boolean change = false;
            for (int i = 0; i < list.size(); i++) {
                INBT value = list.get(i);
                INBT result = modify(name + "[" + i + "]", value, ruleSet);
                list.set(i, result);
                change |= result != value;
            }
            return change ? list.copy() : list;
        } else {
            Rule rule = ruleSet.get(name);
            if (rule == null) {
                return nbt;
            }
            if (nbt instanceof StringNBT) {
                String value = nbt.getString();
                if (value.equals(rule.match)) {
                    System.out.println(" Replaced value for: " + name);
                    return StringNBT.valueOf(rule.replace.toString());
                }
            }
            return nbt;
        }
    }

    private static class RuleSet extends HashMap<String, Rule> {}

    private static class Rule {

        private final Object match;
        private final Object replace;

        private Rule(Object match, Object replace) {
            this.match = match;
            this.replace = replace;
        }
    }
}
