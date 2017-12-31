package jakojaannos.api.mod;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

public abstract class ContentBase {
    private String modId;

    /**
     * Used by auto-registration system for constructing {@link ResourceLocation ResourceLocations}
     */
    protected String getModId() {
        return modId;
    }

    /**
     * INTERNAL USE, DO NOT CALL
     */
    void setModId(String modId) {
        Preconditions.checkState(this.modId == null);
        this.modId = modId;
    }
}
