package com.stool.jetpackstechreborn.client;

import com.stool.jetpackstechreborn.config.JetpackConfigHandler;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class JetpackModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return JetpackConfigHandler::createConfigScreen;
    }
}
