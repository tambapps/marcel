package com.tambapps.marcel.compiler.asm

import org.objectweb.asm.Label

data class LoopContext(val breakLabel: Label, val continueLabel: Label)