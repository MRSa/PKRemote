package net.osdn.gokigen.pkremote.playback.grid;

import android.widget.ImageView;

class ImageGridCellViewHolder
{
    private ImageView imageView;
    private ImageView iconView;

    ImageGridCellViewHolder(ImageView image, ImageView icon)
    {
        this.imageView = image;
        this.iconView = icon;
    }

    ImageView getImageView()
    {
        return (imageView);
    }

    ImageView getIconView()
    {
        return (iconView);
    }

}
