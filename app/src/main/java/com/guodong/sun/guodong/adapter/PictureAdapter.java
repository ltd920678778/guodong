package com.guodong.sun.guodong.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.guodong.sun.guodong.R;
import com.guodong.sun.guodong.activity.LongPictureActivity;
import com.guodong.sun.guodong.activity.MainActivity;
import com.guodong.sun.guodong.activity.MultiGifActivity;
import com.guodong.sun.guodong.activity.MultiPictureActivity;
import com.guodong.sun.guodong.activity.MyApplication;
import com.guodong.sun.guodong.entity.picture.Picture;
import com.guodong.sun.guodong.entity.picture.ThumbImageList;
import com.guodong.sun.guodong.glide.CircleImageTransform;
import com.guodong.sun.guodong.listener.CustomShareListener;
import com.guodong.sun.guodong.listener.OnLoadMoreLisener;
import com.guodong.sun.guodong.uitls.AlxGifHelper;
import com.guodong.sun.guodong.uitls.StringUtils;
import com.guodong.sun.guodong.widget.NineGridImageView;
import com.guodong.sun.guodong.widget.NineGridImageViewAdapter;
import com.guodong.sun.guodong.widget.ResizableImageView;
import com.guodong.sun.guodong.widget.SpacingTextView;
import com.guodong.sun.guodong.widget.SunVideoPlayer;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.shareboard.ShareBoardConfig;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Administrator on 2016/10/10.
 */

public class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnLoadMoreLisener {

    private static final int MULTI_IMAGE = 4;
    private static final int GIF_IMAGE = 2;
    private static final int GIF_MP4_IMAGE = 5;
    private static final int ITEM_IMAGE = 1;
    private static final int LONG_IMAGE = 3;

    private ArrayList<Picture.DataBeanX.DataBean> mPictureLists = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private boolean isLoading;
    private RecyclerView mRecyclerView;

    public PictureAdapter(Context context, RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        Picture.DataBeanX.DataBean.GroupBean bean = mPictureLists.get(position).getGroup();
        if (bean.getMedia_type() == MULTI_IMAGE) {
            if (bean.getIs_multi_image() == 1)
                return MULTI_IMAGE;
        } else if (bean.getMedia_type() == GIF_IMAGE) {
            if (bean.getIs_gif() == 1 && bean.getGifvideo() != null)
                return GIF_MP4_IMAGE;
            else
                return GIF_IMAGE;
        } else if (bean.getMedia_type() == ITEM_IMAGE
                && bean.getMiddle_image().getR_height() < MyApplication.ScreenHeight) {
            return ITEM_IMAGE;
        } else if (bean.getMedia_type() == ITEM_IMAGE
                && bean.getMiddle_image().getR_height() > MyApplication.ScreenHeight) {
            return LONG_IMAGE;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        switch (viewType) {
            case MULTI_IMAGE:
                holder = new MultiItemViewHolder(mInflater.inflate(R.layout.fragment_picture_multi_item, parent, false));
                break;

            case GIF_IMAGE:
                holder = new GifItemViewHolder(mInflater.inflate(R.layout.fragment_picture_gif_item, parent, false));
                break;

            case GIF_MP4_IMAGE:
                holder = new GifMp4ItemViewHolder(mInflater.inflate(R.layout.fragment_picture_gifmp4_item, parent, false));
                break;

            case ITEM_IMAGE:
                holder = new ItemViewHolder(mInflater.inflate(R.layout.fragment_picture_item, parent, false));
                break;

            case LONG_IMAGE:
                holder = new LongItemViewHolder(mInflater.inflate(R.layout.fragment_picture_long_item, parent, false));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Picture.DataBeanX.DataBean.GroupBean bean = mPictureLists.get(position).getGroup();
        switch (holder.getItemViewType()) {
            case MULTI_IMAGE:
                bindMultiImageViewHolder((MultiItemViewHolder) holder, bean);
                break;

            case GIF_IMAGE:
                bindGifImageViewHolder((GifItemViewHolder) holder, bean);
                break;

            case GIF_MP4_IMAGE:
                bindGifMp4ImageViewHolder((GifMp4ItemViewHolder) holder, bean);
                break;

            case ITEM_IMAGE:
                bindItemImageViewHolder((ItemViewHolder) holder, bean);
                break;

            case LONG_IMAGE:
                bindLongImageViewHolder((LongItemViewHolder) holder, bean);
                break;
        }
    }

    /**
     * 绑定长图布局
     *
     * @param holder viewholder
     * @param bean bean
     */
    private void bindLongImageViewHolder(final LongItemViewHolder holder, final Picture.DataBeanX.DataBean.GroupBean bean) {
        displayTopAndBottom(holder, bean);

        // ----------------------------------------------------------

        diaplayLongImage(holder, bean);
    }

    /**
     * 加载长图
     *
     * @param holder viewholder
     * @param bean bean
     */
    private void diaplayLongImage(final LongItemViewHolder holder, final Picture.DataBeanX.DataBean.GroupBean bean) {

        Glide.with(mContext)
                .load(bean.getMiddle_image().getUrl_list().get(0).getUrl())
                .asBitmap()
                .placeholder(R.drawable.ic_default_image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        holder.mImageView.setImageBitmap(Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), 500));
                    }
                });

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LongPictureActivity.startActivity(mContext,
                        bean.getMiddle_image().getUrl_list().get(0).getUrl());
            }
        });

        holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LongPictureActivity.startActivity(mContext,
                        bean.getMiddle_image().getUrl_list().get(0).getUrl());
            }
        });
    }

    /**
     * 绑定单张图片布局
     *
     * @param holder viewholder
     * @param bean bean
     */
    private void bindItemImageViewHolder(final ItemViewHolder holder, Picture.DataBeanX.DataBean.GroupBean bean) {
        displayTopAndBottom(holder, bean);

        // ----------------------------------------------------------

//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.mImageView.getLayoutParams();
//        lp.height = MyApplication.ScreenWidth * bean.getMiddle_image().getR_height() / bean.getMiddle_image().getR_width();
//        holder.mImageView.setLayoutParams(lp);

        final String url = bean.getMiddle_image().getUrl_list().get(0).getUrl();

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> list = new ArrayList<>();
                list.add(url);
                MultiPictureActivity.startActivity(mContext, 0, list);
            }
        });

        Glide.with(mContext)
                .load(url)
                .placeholder(R.drawable.ic_default_image)
                .into(holder.mImageView);
    }

    /**
     * 绑定GIF布局
     *
     * @param holder viewholder
     * @param bean bean
     */
    private void bindGifImageViewHolder(final GifItemViewHolder holder, final Picture.DataBeanX.DataBean.GroupBean bean) {
        displayTopAndBottom(holder, bean);

        // ----------------------------------------------------------

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.mGifImageView.getLayoutParams();
        lp.height = MyApplication.ScreenWidth * bean.getMiddle_image().getR_height() / bean.getMiddle_image().getR_width();
        holder.mGifImageView.setLayoutParams(lp);
//        holder.mImageView.setLayoutParams(lp);

        Glide.with(mContext).load(bean.getMiddle_image().getUrl_list().get(0).getUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mGifImageView);

        // TODO: 2016/12/17
        AlxGifHelper.displayGif(bean.getLarge_image().getUrl_list().get(0).getUrl(),
                holder.mGifImageView, holder.mProgressBar, holder.mTextView);

        final ArrayList<String> listurl = new ArrayList();
        listurl.add(bean.getLarge_image().getUrl_list().get(0).getUrl());

        holder.mGifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiGifActivity.startActivity(mContext, 0, listurl,
                        bean.getMiddle_image().getWidth(), bean.getMiddle_image().getHeight());
            }
        });

    }

    /**
     * 绑定GIF MP4布局
     *
     * @param holder viewholder
     * @param bean bean
     */
    private void bindGifMp4ImageViewHolder(final GifMp4ItemViewHolder holder, final Picture.DataBeanX.DataBean.GroupBean bean) {
        displayTopAndBottom(holder, bean);

        // ----------------------------------------------------------

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.mGifVideo.getLayoutParams();
        lp.height = MyApplication.ScreenWidth * bean.getMiddle_image().getR_height() / bean.getMiddle_image().getR_width();
        holder.mGifVideo.setLayoutParams(lp);

        Glide.with(mContext).load(bean.getMiddle_image().getUrl_list().get(0).getUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mGifVideo.thumbImageView);
        holder.mGifVideo.looping = true; // 循环播放
        holder.mGifVideo.audio = false; // 不获取音频服务

        // TODO: 2016/12/22
        String url = bean.getGifvideo().getMp4_url();
        String path = mContext.getExternalCacheDir().getAbsolutePath()
                + File.separator + AlxGifHelper.getMd5(url) + ".mp4";
        File fileMp4 = new File(path);
        if (fileMp4.exists()) {
            holder.mProgressBar.setVisibility(View.INVISIBLE);
            holder.mGifVideo.setUp(path,
                    JCVideoPlayerStandard.SCREEN_LAYOUT_LIST, "");
        } else {
            AlxGifHelper.startDownLoad(url, fileMp4, new AlxGifHelper.DownLoadTask() {
                @Override
                protected void onStart() {
                    holder.mProgressBar.setVisibility(View.VISIBLE);
                    holder.mProgressBar.setProgress(0);
                }

                @Override
                protected void onLoading(long total, long current) {
                    holder.mProgressBar.setProgress((int) (current * 100 / total));
                }

                @Override
                protected void onSuccess(File target) {
                    holder.mProgressBar.setVisibility(View.INVISIBLE);
                    holder.mGifVideo.setUp(target.getAbsolutePath(),
                            JCVideoPlayerStandard.SCREEN_LAYOUT_LIST, "");
                }

                @Override
                protected void onFailure(Throwable e) {
                    holder.mProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }

        final ArrayList<String> listurl = new ArrayList();
        listurl.add(bean.getLarge_image().getUrl_list().get(0).getUrl());

        holder.mGifVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiGifActivity.startActivity(mContext, 0, listurl,
                        bean.getMiddle_image().getWidth(), bean.getMiddle_image().getHeight());
            }
        });

    }

    /**
     * 绑定多图布局
     *
     * @param holder viewholder
     * @param bean bean
     */
    private void bindMultiImageViewHolder(MultiItemViewHolder holder, Picture.DataBeanX.DataBean.GroupBean bean) {
        displayTopAndBottom(holder, bean);

        // ----------------------------------------------------------

        displayMultiImage(holder, bean);
    }

    private void displayTopAndBottom(PictureViewHolder holder, final Picture.DataBeanX.DataBean.GroupBean bean) {
        holder.user_name.setText(bean.getUser().getName());
        if (TextUtils.isEmpty(bean.getContent()))
            setViewGone(holder.item_content);
        else
            holder.item_content.setText(bean.getContent());
        holder.category_name.setText(bean.getCategory_name());
        holder.item_digg.setText(StringUtils.getStr2W(bean.getDigg_count()));
        holder.item_bury.setText(StringUtils.getStr2W(bean.getBury_count()));
        holder.item_comment.setText(StringUtils.getStr2W(bean.getComment_count()));
        holder.item_share_count.setText(StringUtils.getStr2W(bean.getShare_count()));
        holder.item_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ShareAction((MainActivity)mContext)
                        .setDisplayList(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                                SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE)
                        .setShareboardclickCallback(new ShareBoardlistener() {
                            @Override
                            public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                                if (bean != null) {
                                    ShareContent content = new ShareContent();
                                    content.mTitle = mContext.getResources().getString(R.string.app_name);
                                    content.mText = bean.getContent();
                                    content.mTargetUrl = bean.getShare_url();
                                    new ShareAction((MainActivity)mContext)
                                            .setShareContent(content)
                                            .setPlatform(share_media)
                                            .setCallback(new CustomShareListener((MainActivity)mContext))
                                            .share();
                                }
                            }
                        }).open(new ShareBoardConfig().setMenuItemBackgroundColor(ShareBoardConfig.BG_SHAPE_NONE));
            }
        });
        Glide.with(mContext)
                .load(bean.getUser().getAvatar_url())
                .bitmapTransform(new CircleImageTransform(mContext))
                .into(holder.user_avatar);
    }

    /**
     * NineGirdImageView
     *
     * @param holder viewholder
     * @param bean bean
     */
    private void displayMultiImage(MultiItemViewHolder holder, Picture.DataBeanX.DataBean.GroupBean bean) {

        NineGridImageViewAdapter<ThumbImageList> mAdapter = new NineGridImageViewAdapter<ThumbImageList>() {
            @Override
            protected void onDisplayImage(Context context, ImageView imageView, ThumbImageList s) {
                displayImageView(imageView, s.getUrl());
            }

            @Override
            protected ImageView generateImageView(Context context) {
                return super.generateImageView(context);
            }

            @Override
            protected void onItemImageClick(Context context, int index, List<ThumbImageList> list) {
                ArrayList<String> listUrl = new ArrayList<>();
                for (ThumbImageList thumbImageList : list) {
                    listUrl.add(thumbImageList.getUrl());
                }

                if (list.get(index).is_gif()) {
                    MultiGifActivity.startActivity(context, index, listUrl,
                            list.get(index).getWidth(), list.get(index).getHeight());
                    return;
                }

                MultiPictureActivity.startActivity(context, index,  listUrl);
            }
        };
        holder.mNineGridImageView.setAdapter(mAdapter);
        holder.mNineGridImageView.setImagesData(bean.getThumb_image_list(), bean.getLarge_image_list());
    }

    private void setViewGone(View view) {
        view.setVisibility(View.GONE);
    }

    private void displayImageView(ImageView v, String url) {
        Glide.with(mContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_default_image)
                .into(v);
    }


    @Override
    public int getItemCount() {
        return mPictureLists.size();
    }

    public void addLists(ArrayList<Picture.DataBeanX.DataBean> list) {
        if (mPictureLists.size() != 0 && list.size() != 0) {
            if (mPictureLists.get(0).getGroup().getText().equals(list.get(0).getGroup().getText()))
                return;
        }

        int size = mPictureLists.size();
        if (isLoading) {
            mPictureLists.addAll(list);
            notifyItemRangeInserted(size, list.size());
        } else {
            mPictureLists.addAll(0, list);
            notifyItemRangeInserted(0, list.size());
            mRecyclerView.scrollToPosition(0);
        }
//        notifyDataSetChanged();
    }

    public void clearDuanziList() {
        mPictureLists.clear();
    }

    @Override
    public void onLoadStart() {
        if (isLoading)
            return;
        isLoading = true;
//        notifyItemInserted(getLoadingMoreItemPosition());
    }

    @Override
    public void onLoadFinish() {
        if (!isLoading) return;
//        notifyItemRemoved(getLoadingMoreItemPosition());
        isLoading = false;
    }

    private int getLoadingMoreItemPosition() {
        return isLoading ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    static class PictureViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.picture_item_user_avatar)
        ImageView user_avatar;

        @BindView(R.id.picture_item_user_name)
        TextView user_name;

        @BindView(R.id.picture_item_content)
        SpacingTextView item_content;

        @BindView(R.id.picture_item_category_name)
        TextView category_name;

        @BindView(R.id.picture_item_digg)
        TextView item_digg;

        @BindView(R.id.picture_item_share_count)
        TextView item_share_count;

        @BindView(R.id.picture_item_share)
        RelativeLayout item_share;

        @BindView(R.id.picture_item_bury)
        TextView item_bury;

        @BindView(R.id.picture_item_comment)
        TextView item_comment;

        PictureViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ItemViewHolder extends PictureViewHolder {

        @BindView(R.id.fragment_picture_item_iv)
        ResizableImageView mImageView;

        ItemViewHolder(View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
        }
    }

    static class GifItemViewHolder extends PictureViewHolder {

        @BindView(R.id.fragment_picture_gifview)
        GifImageView mGifImageView;

        @BindView(R.id.fragment_picture_gifview_pb)
        ProgressBar mProgressBar;

        @BindView(R.id.fragment_picture_gifview_tv)
        TextView mTextView;

        GifItemViewHolder(View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
        }
    }

    static class GifMp4ItemViewHolder extends PictureViewHolder {

        @BindView(R.id.fragment_picture_gifmp4)
        SunVideoPlayer mGifVideo;

        @BindView(R.id.fragment_picture_gifmp4_pb)
        ProgressBar mProgressBar;

        GifMp4ItemViewHolder(View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
        }
    }

    static class LongItemViewHolder extends PictureViewHolder {

        @BindView(R.id.fragment_picture_item_iv)
        ImageView mImageView;

        @BindView(R.id.fragment_picture_item_ll)
        LinearLayout mLinearLayout;

        LongItemViewHolder(View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
        }
    }

    static class MultiItemViewHolder extends PictureViewHolder {

        @BindView(R.id.picture_multi_nine)
        NineGridImageView mNineGridImageView;

        MultiItemViewHolder(View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
        }
    }

}
