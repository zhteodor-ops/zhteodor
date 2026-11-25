package com.zhstudio.jerrymod;

import com.zhstudio.jerrymod.network.JerryNetwork;
import net.minecraftforge.fml.common.Mod;

/**
 * Jerry Mod - one player can become Jerry the dog.
 *  /consoleJerry NAME  -> become Jerry (only one Jerry per world)
 *  F                   -> demonic Jerry (press again to go back)
 *  J                   -> skinwalker Jerry (press again to go back)
 *  /unJerry            -> stop being Jerry
 */
@Mod(JerryMod.MODID)
public class JerryMod {
    public static final String MODID = "jerrymod";

    public JerryMod() {
        JerryNetwork.register();
    }
}
