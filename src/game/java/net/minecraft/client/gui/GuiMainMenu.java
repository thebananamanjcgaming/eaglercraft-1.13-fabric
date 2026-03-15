package net.minecraft.client.gui;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import net.lax1dude.eaglercraft.v1_8.EagRuntime;
import net.lax1dude.eaglercraft.v1_8.EagUtils;
import net.lax1dude.eaglercraft.v1_8.EaglerInputStream;
import net.lax1dude.eaglercraft.v1_8.EaglercraftRandom;
import net.lax1dude.eaglercraft.v1_8.Mouse;
import net.lax1dude.eaglercraft.v1_8.crypto.SHA1Digest;
import net.lax1dude.eaglercraft.v1_8.internal.EnumCursorType;
import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.lax1dude.eaglercraft.v1_8.opengl.EaglercraftGPU;
import net.lax1dude.eaglercraft.v1_8.opengl.GlStateManager;
import net.lax1dude.eaglercraft.v1_8.opengl.WorldRenderer;
import net.lax1dude.eaglercraft.v1_8.profile.GuiScreenEditProfile;
import net.lax1dude.eaglercraft.v1_8.sp.SingleplayerServerController;
import net.lax1dude.eaglercraft.v1_8.sp.gui.GuiScreenDemoPlayWorldSelection;
import net.lax1dude.eaglercraft.v1_8.sp.gui.GuiScreenIntegratedServerBusy;
import net.lax1dude.eaglercraft.v1_8.sp.gui.GuiScreenIntegratedServerStartup;
import net.lax1dude.eaglercraft.v1_8.update.GuiUpdateCheckerOverlay;
import net.lax1dude.eaglercraft.v1_8.update.GuiUpdateVersionSlot;
import net.lax1dude.eaglercraft.v1_8.update.UpdateCertificate;
import net.lax1dude.eaglercraft.v1_8.update.UpdateService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.ISaveFormat;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
   private static final Logger logger = LogManager.getLogger();
   private static final EaglercraftRandom RANDOM = new EaglercraftRandom();
   private float updateCounter;
   private boolean isDefault;
   private static final int lendef = 5987;
   private static final byte[] sha1def = new byte[]{-107, 77, 108, 49, 11, -100, -8, -119, -1, -100, -85, -55, 18, -69, -107, 113, -93, -101, -79, 32};
   private String splashText;
   private GuiButton buttonResetDemo;
   private int panoramaTimer;
   private static DynamicTexture viewportTexture = null;
   private boolean field_175375_v = true;
   private String openGLWarning1;
   private String openGLWarning2;
   private static final ResourceLocation splashTexts = new ResourceLocation("texts/splashes.txt");
   private static final ResourceLocation minecraftTitleTextures = new ResourceLocation("textures/gui/title/minecraft.png");
   private static final ResourceLocation minecraftTitleBlurFlag = new ResourceLocation("textures/gui/title/background/enable_blur.txt");
   private static final ResourceLocation eaglerGuiTextures = new ResourceLocation("eagler:gui/eagler_gui.png");
   private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{new ResourceLocation("textures/gui/title/background/panorama_0.png"), new ResourceLocation("textures/gui/title/background/panorama_1.png"), new ResourceLocation("textures/gui/title/background/panorama_2.png"), new ResourceLocation("textures/gui/title/background/panorama_3.png"), new ResourceLocation("textures/gui/title/background/panorama_4.png"), new ResourceLocation("textures/gui/title/background/panorama_5.png")};
   private int field_92024_r;
   private int field_92023_s;
   private int field_92022_t;
   private int field_92021_u;
   private int field_92020_v;
   private int field_92019_w;
   private static ResourceLocation backgroundTexture = null;
   private GuiUpdateCheckerOverlay updateCheckerOverlay;
   private GuiButton downloadOfflineButton;
   private boolean enableBlur = true;
   private boolean shouldReload = false;
   private static GuiMainMenu instance = null;

   public GuiMainMenu() {
      instance = this;
      this.splashText = "missingno";
      this.updateCheckerOverlay = new GuiUpdateCheckerOverlay(false, this);
      BufferedReader var1 = null;

      try {
         ArrayList var2 = Lists.newArrayList();
         var1 = new BufferedReader(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(splashTexts).getInputStream(), Charsets.UTF_8));

         String var3;
         while((var3 = var1.readLine()) != null) {
            var3 = var3.trim();
            if (!var3.isEmpty()) {
               var2.add(var3);
            }
         }

         if (!var2.isEmpty()) {
            do {
               this.splashText = (String)var2.get(RANDOM.nextInt(var2.size()));
            } while(this.splashText.hashCode() == 125780783);
         }
      } catch (IOException var12) {
      } finally {
         if (var1 != null) {
            try {
               var1.close();
            } catch (IOException var11) {
            }
         }

      }

      this.updateCounter = RANDOM.nextFloat();
      this.reloadResourceFlags();
   }

   private void reloadResourceFlags() {
      byte[] var1;
      if (Minecraft.getMinecraft().isDemo()) {
         this.isDefault = false;
      } else if (!EagRuntime.getConfiguration().isEnableMinceraft()) {
         this.isDefault = false;
      } else {
         try {
            var1 = EaglerInputStream.inputStreamToBytesQuiet(Minecraft.getMinecraft().getResourceManager().getResource(minecraftTitleTextures).getInputStream());
            if (var1 != null && var1.length == 5987) {
               SHA1Digest var2 = new SHA1Digest();
               byte[] var3 = new byte[20];
               var2.update(var1, 0, var1.length);
               var2.doFinal(var3, 0);
               this.isDefault = Arrays.equals(var3, sha1def);
            } else {
               this.isDefault = false;
            }
         } catch (IOException var6) {
            this.isDefault = false;
         }
      }

      this.enableBlur = true;

      try {
         var1 = EaglerInputStream.inputStreamToBytesQuiet(Minecraft.getMinecraft().getResourceManager().getResource(minecraftTitleBlurFlag).getInputStream());
         if (var1 != null) {
            String[] var7 = EagUtils.linesArray(new String(var1, StandardCharsets.UTF_8));

            for(int var8 = 0; var8 < var7.length; ++var8) {
               String var4 = var7[var8];
               if (var4.startsWith("enable_blur=")) {
                  var4 = var4.substring(12).trim();
                  this.enableBlur = var4.equals("1") || var4.equals("true");
                  break;
               }
            }
         }
      } catch (IOException var5) {
      }

   }

   public static void doResourceReloadHack() {
      if (instance != null) {
         instance.shouldReload = true;
      }

   }

   public void updateScreen() {
      ++this.panoramaTimer;
      if (this.downloadOfflineButton != null) {
         this.downloadOfflineButton.enabled = !UpdateService.shouldDisableDownloadButton();
      }

      if (this.shouldReload) {
         this.reloadResourceFlags();
         this.shouldReload = false;
      }

   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   protected void keyTyped(char var1, int var2) {
   }

   public void initGui() {
      if (viewportTexture == null) {
         viewportTexture = new DynamicTexture(256, 256);
         backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", viewportTexture);
      }

      this.updateCheckerOverlay.setResolution(this.mc, this.width, this.height);
      Calendar var1 = EagRuntime.getLocaleCalendar();
      var1.setTime(new Date());
      if (var1.get(2) + 1 == 12 && var1.get(5) == 24) {
         this.splashText = "Merry X-mas!";
      } else if (var1.get(2) + 1 == 1 && var1.get(5) == 1) {
         this.splashText = "Happy new year!";
      } else if (var1.get(2) + 1 == 10 && var1.get(5) == 31) {
         this.splashText = "OOoooOOOoooo! Spooky!";
      }

      int var2 = this.height / 4 + 48;
      boolean var3 = !"TBMJCG".equalsIgnoreCase("TBMJCG");
      if (var3 && "Made by TBMJCG and Microsoft" != null && "Made by TBMJCG and Microsoft".length() > 0) {
         var2 += 11;
      }

      if (this.mc.isDemo()) {
         this.addDemoButtons(var2, 24);
      } else {
         this.addSingleplayerMultiplayerButtons(var2, 24);
      }

      this.buttonList.add(new GuiButton(0, this.width / 2 - 100, var2 + 72 + 12, 98, 20, I18n.format("menu.options", new Object[0])));
      this.buttonList.add(new GuiButton(4, this.width / 2 + 2, var2 + 72 + 12, 98, 20, I18n.format("menu.editProfile", new Object[0])));
      this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, var2 + 72 + 12));
      if (var3) {
         this.openGLWarning1 = "Minecraft 1.13/Fabric";
         this.openGLWarning2 = "Made by TBMJCG and Microsoft";
         boolean var4 = this.openGLWarning2 != null && this.openGLWarning2.length() > 0;
         this.field_92023_s = this.fontRendererObj.getStringWidth(this.openGLWarning1);
         this.field_92024_r = this.fontRendererObj.getStringWidth(this.openGLWarning2);
         int var5 = Math.max(this.field_92023_s, this.field_92024_r);
         this.field_92022_t = (this.width - var5) / 2;
         this.field_92021_u = ((GuiButton)this.buttonList.get(0)).yPosition - (var4 ? 32 : 21);
         this.field_92020_v = this.field_92022_t + var5;
         this.field_92019_w = this.field_92021_u + (var4 ? 24 : 11);
      }

      this.mc.func_181537_a(false);
   }

   private void addSingleplayerMultiplayerButtons(int var1, int var2) {
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, var1, I18n.format("menu.singleplayer", new Object[0])));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 100, var1 + var2 * 1, I18n.format("menu.multiplayer", new Object[0])));
      if (EagRuntime.getConfiguration().isEnableDownloadOfflineButton() && (EagRuntime.getConfiguration().getDownloadOfflineButtonLink() != null || !EagRuntime.isOfflineDownloadURL() && UpdateService.supported() && UpdateService.getClientSignatureData() != null)) {
         this.buttonList.add(this.downloadOfflineButton = new GuiButton(15, this.width / 2 - 100, var1 + var2 * 2, I18n.format("update.downloadOffline", new Object[0])));
         this.downloadOfflineButton.enabled = !UpdateService.shouldDisableDownloadButton();
      }

      int modsIndex = this.downloadOfflineButton != null ? 3 : 2;
      this.buttonList.add(new GuiButton(16, this.width / 2 - 100, var1 + var2 * modsIndex, "Mods"));

   }

   private void addDemoButtons(int var1, int var2) {
      this.buttonList.add(new GuiButton(11, this.width / 2 - 100, var1, I18n.format("menu.playdemo", new Object[0])));
      this.buttonList.add(this.buttonResetDemo = new GuiButton(12, this.width / 2 - 100, var1 + var2 * 1, I18n.format("menu.resetdemo", new Object[0])));
      this.buttonResetDemo.enabled = this.mc.gameSettings.hasCreatedDemoWorld;
   }

   protected void actionPerformed(GuiButton var1) {
      if (var1.id == 0) {
         this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
      }

      if (var1.id == 5) {
         this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
      }

      if (var1.id == 1) {
         this.mc.displayGuiScreen(new GuiScreenIntegratedServerStartup(this));
      }

      if (var1.id == 2) {
         this.mc.displayGuiScreen(new GuiMultiplayer(this));
      }

      if (var1.id == 4) {
         this.mc.displayGuiScreen(new GuiScreenEditProfile(this));
      }

      if (var1.id == 14) {
         EagRuntime.openLink("https://gitlab.com/lax1dude/eaglercraftx-1.8");
      }

      if (var1.id == 11) {
         this.mc.displayGuiScreen(new GuiScreenDemoPlayWorldSelection(this));
      }

      if (var1.id == 12) {
         GuiYesNo var2 = GuiSelectWorld.func_152129_a(this, "Demo World", 12);
         this.mc.displayGuiScreen(var2);
      }

      if (var1.id == 15 && EagRuntime.getConfiguration().isEnableDownloadOfflineButton()) {
         String var3 = EagRuntime.getConfiguration().getDownloadOfflineButtonLink();
         if (var3 != null) {
            EagRuntime.openLink(var3);
         } else {
            UpdateService.quine();
         }
      }


         if (var1.id == 16) {
            this.mc.displayGuiScreen(new GuiModMenu(this));
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
         }
   }

   public void confirmClicked(boolean var1, int var2) {
      if (var1 && var2 == 12) {
         this.mc.gameSettings.hasCreatedDemoWorld = false;
         this.mc.gameSettings.saveOptions();
         ISaveFormat var3 = this.mc.getSaveLoader();
         var3.deleteWorldDirectory("Demo World");
         this.mc.displayGuiScreen(new GuiScreenIntegratedServerBusy(this, "singleplayer.busy.deleting", "singleplayer.failed.deleting", SingleplayerServerController::isReady));
      } else {
         this.mc.displayGuiScreen(this);
      }

   }

   private void drawPanorama(int var1, int var2, float var3) {
      Tessellator var4 = Tessellator.getInstance();
      WorldRenderer var5 = var4.getWorldRenderer();
      GlStateManager.matrixMode(5889);
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      if (this.enableBlur) {
         GlStateManager.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
      } else {
         GlStateManager.gluPerspective(85.0F, (float)this.width / (float)this.height, 0.05F, 10.0F);
      }

      GlStateManager.matrixMode(5888);
      GlStateManager.pushMatrix();
      GlStateManager.loadIdentity();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
      if (this.enableBlur) {
         GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      }

      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.disableCull();
      GlStateManager.depthMask(false);
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      int var6 = this.enableBlur ? 8 : 1;

      for(int var7 = 0; var7 < var6 * var6; ++var7) {
         GlStateManager.pushMatrix();
         float var8 = ((float)(var7 % var6) / (float)var6 - 0.5F) / 64.0F;
         float var9 = ((float)(var7 / var6) / (float)var6 - 0.5F) / 64.0F;
         float var10 = 0.0F;
         GlStateManager.translate(var8, var9, var10);
         GlStateManager.rotate(MathHelper.sin(((float)this.panoramaTimer + var3) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(-((float)this.panoramaTimer + var3) * 0.1F, 0.0F, 1.0F, 0.0F);

         for(int var11 = 0; var11 < 6; ++var11) {
            GlStateManager.pushMatrix();
            if (var11 == 1) {
               GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var11 == 2) {
               GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var11 == 3) {
               GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            }

            if (var11 == 4) {
               GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }

            if (var11 == 5) {
               GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }

            this.mc.getTextureManager().bindTexture(titlePanoramaPaths[var11]);
            var5.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            int var12 = 255 / (var7 + 1);
            var5.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, var12).endVertex();
            var5.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, var12).endVertex();
            var5.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, var12).endVertex();
            var5.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, var12).endVertex();
            var4.draw();
            GlStateManager.popMatrix();
         }

         GlStateManager.popMatrix();
         GlStateManager.colorMask(true, true, true, false);
      }

      var5.setTranslation(0.0D, 0.0D, 0.0D);
      GlStateManager.colorMask(true, true, true, true);
      GlStateManager.matrixMode(5889);
      GlStateManager.popMatrix();
      GlStateManager.matrixMode(5888);
      GlStateManager.popMatrix();
      GlStateManager.depthMask(true);
      GlStateManager.enableCull();
      GlStateManager.enableDepth();
   }

   private void rotateAndBlurSkybox(float var1) {
      this.mc.getTextureManager().bindTexture(backgroundTexture);
      EaglercraftGPU.glTexParameteri(3553, 10241, 9729);
      EaglercraftGPU.glTexParameteri(3553, 10240, 9729);
      EaglercraftGPU.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.colorMask(true, true, true, false);
      Tessellator var2 = Tessellator.getInstance();
      WorldRenderer var3 = var2.getWorldRenderer();
      var3.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      GlStateManager.disableAlpha();
      byte var4 = 3;

      for(int var5 = 0; var5 < var4; ++var5) {
         float var6 = 1.0F / (float)(var5 + 1);
         int var7 = this.width;
         int var8 = this.height;
         float var9 = (float)(var5 - var4 / 2) / 256.0F;
         var3.pos((double)var7, (double)var8, (double)this.zLevel).tex((double)(0.0F + var9), 1.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
         var3.pos((double)var7, 0.0D, (double)this.zLevel).tex((double)(1.0F + var9), 1.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
         var3.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(1.0F + var9), 0.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
         var3.pos(0.0D, (double)var8, (double)this.zLevel).tex((double)(0.0F + var9), 0.0D).color(1.0F, 1.0F, 1.0F, var6).endVertex();
      }

      var2.draw();
      GlStateManager.enableAlpha();
      GlStateManager.colorMask(true, true, true, true);
   }

   private void renderSkybox(int var1, int var2, float var3) {
      GlStateManager.viewport(0, 0, 256, 256);
      this.drawPanorama(var1, var2, var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      this.rotateAndBlurSkybox(var3);
      GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
      float var4 = this.width > this.height ? 120.0F / (float)this.width : 120.0F / (float)this.height;
      float var5 = (float)this.height * var4 / 256.0F;
      float var6 = (float)this.width * var4 / 256.0F;
      int var7 = this.width;
      int var8 = this.height;
      Tessellator var9 = Tessellator.getInstance();
      WorldRenderer var10 = var9.getWorldRenderer();
      var10.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var10.pos(0.0D, (double)var8, (double)this.zLevel).tex((double)(0.5F - var5), (double)(0.5F + var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var10.pos((double)var7, (double)var8, (double)this.zLevel).tex((double)(0.5F - var5), (double)(0.5F - var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var10.pos((double)var7, 0.0D, (double)this.zLevel).tex((double)(0.5F + var5), (double)(0.5F - var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var10.pos(0.0D, 0.0D, (double)this.zLevel).tex((double)(0.5F + var5), (double)(0.5F + var6)).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
      var9.draw();
   }

   public void drawScreen(int var1, int var2, float var3) {
      GlStateManager.disableAlpha();
      if (this.enableBlur) {
         this.renderSkybox(var1, var2, var3);
      } else {
         this.drawPanorama(var1, var2, var3);
      }

      GlStateManager.enableAlpha();
      short var4 = 274;
      int var5 = this.width / 2 - var4 / 2;
      byte var6 = 30;
      this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
      this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
      this.mc.getTextureManager().bindTexture(minecraftTitleTextures);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      boolean var7 = (double)this.updateCounter < 1.0E-4D;
      if (this.isDefault) {
         var7 = !var7;
      }

      if (var7) {
         this.drawTexturedModalRect(var5 + 0, var6 + 0, 0, 0, 99, 44);
         this.drawTexturedModalRect(var5 + 99, var6 + 0, 129, 0, 27, 44);
         this.drawTexturedModalRect(var5 + 99 + 26, var6 + 0, 126, 0, 3, 44);
         this.drawTexturedModalRect(var5 + 99 + 26 + 3, var6 + 0, 99, 0, 26, 44);
         this.drawTexturedModalRect(var5 + 154, var6 + 0, 0, 45, 155, 44);
      } else {
         this.drawTexturedModalRect(var5 + 0, var6 + 0, 0, 0, 155, 44);
         this.drawTexturedModalRect(var5 + 155, var6 + 0, 0, 45, 155, 44);
      }

      boolean var8 = this.openGLWarning1 != null && this.openGLWarning1.length() > 0 || this.openGLWarning2 != null && this.openGLWarning2.length() > 0;
      if (var8) {
         drawRect(this.field_92022_t - 3, this.field_92021_u - 3, this.field_92020_v + 3, this.field_92019_w, 1428160512);
         if (this.openGLWarning1 != null) {
            this.drawString(this.fontRendererObj, this.openGLWarning1, this.field_92022_t, this.field_92021_u, -1);
         }

         if (this.openGLWarning2 != null) {
            this.drawString(this.fontRendererObj, this.openGLWarning2, (this.width - this.field_92024_r) / 2, this.field_92021_u + 12, -1);
         }
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
      GlStateManager.rotate(var8 ? -12.0F : -20.0F, 0.0F, 0.0F, 1.0F);
      float var9 = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * 3.1415927F * 2.0F) * 0.1F);
      var9 = var9 * 100.0F / (float)(this.fontRendererObj.getStringWidth(this.splashText) + 32);
      if (var8) {
         var9 *= 0.8F;
      }

      GlStateManager.scale(var9, var9, var9);
      this.drawCenteredString(this.fontRendererObj, this.splashText, 0, -8, -256);
      GlStateManager.popMatrix();
      String var10 = "Minecraft 1.13/Fabric";
      if (this.mc.isDemo()) {
         var10 = var10 + " Demo";
      }

      this.drawString(this.fontRendererObj, var10, 2, this.height - 20, -1);
      var10 = "MCP-20180718-1.13";
      this.drawString(this.fontRendererObj, var10, 2, this.height - 10, -1);
      String var11 = "";
      this.drawString(this.fontRendererObj, var11, this.width - this.fontRendererObj.getStringWidth(var11) - 2, this.height - 20, -1);
      var11 = "Resources Copyright Mojang AB";
      if (this.mc.isDemo()) {
         var11 = "Copyright Mojang AB. Do not distribute!";
      }

      this.drawString(this.fontRendererObj, var11, this.width - this.fontRendererObj.getStringWidth(var11) - 2, this.height - 10, -1);
      int var12;
      if (!this.mc.isDemo()) {
         GlStateManager.pushMatrix();
         GlStateManager.scale(0.75F, 0.75F, 0.75F);
         int var13 = 0;
         var12 = 0;
         var11 = "Collector's Edition";
         if (var11 != null) {
            var13 = this.fontRendererObj.getStringWidth(var11);
            var12 += 10;
         }

         var11 = "Optifine Pack";
         if (var11 != null) {
            var13 = Math.max(var13, this.fontRendererObj.getStringWidth(var11));
            var12 += 10;
         }

         if (var13 > 0) {
            drawRect(0, 0, var13 + 6, var12 + 4, 1428160512);
            var11 = "Collector's Edition";
            if (var11 != null) {
               var13 = this.fontRendererObj.getStringWidth(var11);
               this.drawString(this.fontRendererObj, var11, 3, 3, -103);
            }

            var11 = "Optifine Pack";
            if (var11 != null) {
               Math.max(var13, this.fontRendererObj.getStringWidth(var11));
               this.drawString(this.fontRendererObj, var11, 3, 13, -103);
            }
         }

         if (EagRuntime.getConfiguration().isEnableSignatureBadge()) {
            UpdateCertificate var14 = UpdateService.getClientCertificate();
            GlStateManager.scale(0.66667F, 0.66667F, 0.66667F);
            if (var14 != null) {
               var11 = I18n.format("update.digitallySigned", new Object[]{GuiUpdateVersionSlot.dateFmt.format(new Date(var14.sigTimestamp))});
            } else {
               var11 = I18n.format("update.signatureInvalid", new Object[0]);
            }

            var13 = this.fontRendererObj.getStringWidth(var11) + 14;
            drawRect((this.width * 2 - var13) / 2, 0, (this.width * 2 - var13) / 2 + var13, 12, 855638016);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawString(this.fontRendererObj, var11, (this.width * 2 - var13) / 2 + 12, 2, var14 != null ? -103 : -43691);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            this.mc.getTextureManager().bindTexture(eaglerGuiTextures);
            this.drawTexturedModalRect((int)((float)((this.width * 2 - var13) / 2) / 0.6F) + 2, 1, var14 != null ? 32 : 16, 0, 16, 16);
         }

         GlStateManager.popMatrix();
      }

      String var15 = "Credits";
      var12 = this.fontRendererObj.getStringWidth(var15) * 3 / 4;
      if (var1 >= this.width - var12 - 4 && var1 <= this.width && var2 >= 0 && var2 <= 9) {
         Mouse.showCursor(EnumCursorType.HAND);
         drawRect(this.width - var12 - 4, 0, this.width, 10, 1426063513);
      } else {
         drawRect(this.width - var12 - 4, 0, this.width, 10, 1428160512);
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate((float)(this.width - var12 - 2), 2.0F, 0.0F);
      GlStateManager.scale(0.75F, 0.75F, 0.75F);
      this.drawString(this.fontRendererObj, var15, 0, 0, 16777215);
      GlStateManager.popMatrix();
      this.updateCheckerOverlay.drawScreen(var1, var2, var3);
      super.drawScreen(var1, var2, var3);
   }

   protected void mouseClicked(int var1, int var2, int var3) {
      if (var3 == 0) {
         String var4 = "Credits";
         int var5 = this.fontRendererObj.getStringWidth(var4) * 3 / 4;
         if (var1 >= this.width - var5 - 4 && var1 <= this.width && var2 >= 0 && var2 <= 10) {
            String var6 = EagRuntime.getResourceString("/assets/eagler/CREDITS.txt");
            if (var6 != null) {
               EagRuntime.openCreditsPopup(var6);
            }

            this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            return;
         }
      }

      this.updateCheckerOverlay.mouseClicked(var1, var2, var3);
      super.mouseClicked(var1, var2, var3);
   }
}