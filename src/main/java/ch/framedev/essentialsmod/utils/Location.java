package ch.framedev.essentialsmod.utils;



/*
 * ch.framedev.essentialsmod.utils
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 13.01.2025 18:12
 */

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class Location {

    private String dimension;
    private int x,y,z;

    public Location(String dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location(int x, int y, int z) {
        this.dimension = null;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public ResourceKey<Level> getDimensionResource() {
        if(dimension == null) return null;
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimension));
    }

    public ServerLevel getServerLevel(ServerPlayer serverPlayer) {
        if(dimension == null) return null;
        if(serverPlayer.getServer() == null) return null;
        return dimension!= null? serverPlayer.getServer().getLevel(getDimensionResource()) : null;
    }
}
