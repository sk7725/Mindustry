package io.anuke.mindustry.desktop;

import io.anuke.arc.*;
import io.anuke.arc.files.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.mod.*;
import io.anuke.mindustry.mod.Mods.*;
import org.graalvm.polyglot.*;

import java.io.*;

public class GraalScripts extends Scripts{
    private static final Class[] denied = {FileHandle.class, InputStream.class, File.class, Scripts.class, Files.class, ClassAccess.class};
    private final Context context;
    private final String wrapper;
    private final Context.Builder builder;
    private Context console;

    public GraalScripts(){
        Time.mark();
        builder = Context.newBuilder("js").allowHostClassLookup(ClassAccess.allowedClassNames::contains);

        HostAccess.Builder hb = HostAccess.newBuilder();
        hb.allowPublicAccess(true);
        for(Class c : denied){
            hb.denyAccess(c);
        }
        builder.allowHostAccess(hb.build());
        builder.allowExperimentalOptions(true).option("js.syntax-extensions", "true");

        context = builder.build();
        wrapper = Core.files.internal("scripts/wrapper.js").readString();

        run(Core.files.internal("scripts/global.js").readString());
        Log.info("Time to load script engine: {0}", Time.elapsed());
    }

    @Override
    public void run(LoadedMod mod, FileHandle file){
        run(wrapper.replace("$SCRIPT_NAME$", mod.name + "_" +file.nameWithoutExtension().replace("-", "_").replace(" ", "_")).replace("$CODE$", file.readString()));
    }

    @Override
    public String runConsole(String text){
        if(console == null){
            console = builder.build();
        }
        return console.eval("js", text).toString();
    }

    private void run(String script){
        context.eval("js", script);
    }
}
