package com.tambapps.marcel.android.marshell.repl;

import com.tambapps.marcel.android.marshell.repl.console.Printer;

import marcel.lang.Binding;
import marcel.lang.Script;

public abstract class MarshellScript extends Script {

    private Printer printer;

    public MarshellScript() {
        super();
    }

    public MarshellScript(Binding binding) {
        super(binding);
    }


    @Override
    public void print(Object o) {
        printer.print(o);
    }

    @Override
    public void println() {
        printer.println();
    }

    @Override
    public void println(Object o) {
        printer.println(o);
    }

    public void setPrinter(Printer printer) {
        this.printer = printer;
    }
}
