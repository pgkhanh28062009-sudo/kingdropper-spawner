package com.example.addon;

import com.example.addon.modules.KingMCSpawnerv2;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class Addon extends MeteorAddon {
    public static final Category CATEGORY = new Category("KingMC");

    @Override
    public void onInitialize() {
        // Khai báo Module an toàn khi Meteor khởi chạy
        Modules.get().add(new KingMCSpawnerv2(CATEGORY));
    }

    @Override
    public void onRegisterCategories() {
        // Đăng ký danh mục hiển thị trên GUI Meteor
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("pgkhanh28062009-sudo", "kingdropper-spawner");
    }
}
