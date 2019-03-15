package net.osdn.gokigen.pkremote.playback.grid;

import android.widget.ImageView;

class ImageGridCellViewHolder
{
    private ImageView imageView;
    private ImageView iconView;
    private ImageView selectView;

    ImageGridCellViewHolder(ImageView image, ImageView icon, ImageView selection)
    {
        this.imageView = image;
        this.iconView = icon;
        this.selectView = selection;
    }

    ImageView getImageView()
    {
        return (imageView);
    }

    ImageView getIconView()
    {
        return (iconView);
    }

    ImageView getSelectView()
    {
        return (selectView);
    }

}
