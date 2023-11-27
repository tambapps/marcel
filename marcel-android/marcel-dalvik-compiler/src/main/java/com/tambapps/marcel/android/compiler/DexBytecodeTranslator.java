package com.tambapps.marcel.android.compiler;

import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.cf.direct.StdAttributeFactory;
import com.android.dx.command.dexer.DxContext;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CfOptions;
import com.android.dx.dex.cf.CfTranslator;
import com.android.dx.dex.code.PositionList;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.dex.file.DexFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class DexBytecodeTranslator {


    private final DexOptions dexOptions = new DexOptions();
    private final CfOptions cfOptions = new CfOptions();
    private final DxContext dxContext = new DxContext();

    private final DexFile dexFile;

    public static byte[] translate(String className, byte[] originalByteCode) throws IOException {
        DexBytecodeTranslator translator = new DexBytecodeTranslator();
        translator.addClass(className, originalByteCode);
        return translator.getDexBytes();
    }

    public DexBytecodeTranslator() {
        dexOptions.minSdkVersion = BuildConfig.MIN_SDK_VERSION;
        cfOptions.positionInfo = PositionList.LINES;
        cfOptions.localInfo = true;
        cfOptions.strictNameCheck = true;
        cfOptions.optimize = false;
        cfOptions.optimizeListFile = null;
        cfOptions.dontOptimizeListFile = null;
        cfOptions.statistics = false;
        dexFile = new DexFile(dexOptions);
    }

    public void addClass(String className, byte[] javaByteCode) {
        final DirectClassFile directClassFile = new DirectClassFile(javaByteCode,
                className + ".class", false);
        directClassFile.setAttributeFactory(StdAttributeFactory.THE_ONE);
        directClassFile.getMagic(); // triggers the actual parsing

        final ClassDefItem classDefItem = CfTranslator.translate(
                dxContext,
                directClassFile,
                javaByteCode,
                cfOptions,
                dexOptions,
                dexFile
        );
        dexFile.add(classDefItem);
    }
    public byte[] getDexBytes() throws IOException {
        return dexFile.toDex(new OutputStreamWriter(new ByteArrayOutputStream()), false);
    }
}
