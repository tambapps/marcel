package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.bytecode.BytecodeGenerator
import com.tambapps.marcel.parser.ast.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class MarcelCompilerTest {

    @Disabled
    @Test
    fun test() {
        val name = "Test"
        // TODO rewrite it
        /*
        val node = TokenNode(TokenNodeType.SCRIPT, name, mutableListOf(
            TokenNode(TokenNodeType.FUNCTION_CALL, "println").apply {
                addChild(TokenNode(TokenNodeType.INTEGER, "8"))
            }
        ))
        val bytecodeGenerator = BytecodeGenerator()

        Files.write(Paths.get("/home/nfonkoua/Downloads/marcel/$name.class"), bytecodeGenerator.generate(node))

         */
    }
}