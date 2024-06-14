package com.odysee.app.ui.findcontent;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.common.collect.Ordering;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.odysee.app.callable.ChannelLiveStatus;
import com.odysee.app.OdyseeApp;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Code;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.odysee.app.MainActivity;
import com.odysee.app.R;
import com.odysee.app.adapter.ChatMessageListAdapter;
import com.odysee.app.adapter.ClaimListAdapter;
import com.odysee.app.adapter.CommentItemDecoration;
import com.odysee.app.adapter.CommentListAdapter;
import com.odysee.app.adapter.InlineChannelSpinnerAdapter;
import com.odysee.app.adapter.TagListAdapter;
import com.odysee.app.callable.LighthouseSearch;
import com.odysee.app.callable.BuildCommentReactOptions;
import com.odysee.app.exceptions.LbryRequestException;
import com.odysee.app.exceptions.LbryResponseException;
import com.odysee.app.listener.FilePickerListener;
import com.odysee.app.model.OdyseeCollection;
import com.odysee.app.model.lbryinc.CreatorSetting;
import com.odysee.app.model.lbryinc.CustomBlockRule;
import com.odysee.app.runnable.ReactToComment;
import com.odysee.app.dialog.RepostClaimDialogFragment;
import com.odysee.app.dialog.CreateSupportDialogFragment;
import com.odysee.app.exceptions.ApiCallException;
import com.odysee.app.exceptions.LbryUriException;
import com.odysee.app.exceptions.LbryioRequestException;
import com.odysee.app.exceptions.LbryioResponseException;
import com.odysee.app.listener.FetchClaimsListener;
import com.odysee.app.listener.PIPModeListener;
import com.odysee.app.listener.ScreenOrientationListener;
import com.odysee.app.listener.StoragePermissionListener;
import com.odysee.app.model.Claim;
import com.odysee.app.model.ClaimCacheKey;
import com.odysee.app.model.Comment;
import com.odysee.app.model.Fee;
import com.odysee.app.model.LbryFile;
import com.odysee.app.model.Reactions;
import com.odysee.app.model.Tag;
import com.odysee.app.model.UrlSuggestion;
import com.odysee.app.model.lbryinc.Reward;
import com.odysee.app.model.lbryinc.Subscription;
import com.odysee.app.tasks.BufferEventTask;
import com.odysee.app.tasks.CommentCreateTask;
import com.odysee.app.tasks.CommentListHandler;
import com.odysee.app.tasks.CommentListTask;
import com.odysee.app.tasks.ReadTextFileTask;
import com.odysee.app.tasks.claim.AbandonHandler;
import com.odysee.app.tasks.claim.AbandonStreamTask;
import com.odysee.app.tasks.claim.ClaimListResultHandler;
import com.odysee.app.tasks.claim.ClaimListTask;
import com.odysee.app.tasks.claim.ClaimSearchResultHandler;
import com.odysee.app.tasks.claim.PurchaseListTask;
import com.odysee.app.tasks.claim.ResolveResultHandler;
import com.odysee.app.tasks.claim.ResolveTask;
import com.odysee.app.tasks.file.FileListTask;
import com.odysee.app.tasks.lbryinc.ChannelSubscribeTask;
import com.odysee.app.tasks.lbryinc.ClaimRewardTask;
import com.odysee.app.tasks.lbryinc.FetchStatCountTask;
import com.odysee.app.ui.BaseFragment;
import com.odysee.app.ui.channel.ChannelCreateDialogFragment;
import com.odysee.app.ui.controls.SolidIconView;
import com.odysee.app.utils.Comments;
import com.odysee.app.utils.Helper;
import com.odysee.app.utils.Lbry;
import com.odysee.app.utils.LbryAnalytics;
import com.odysee.app.utils.LbryUri;
import com.odysee.app.utils.Lbryio;
import com.odysee.app.utils.PlayerManager;
import com.odysee.app.utils.Utils;
import com.odysee.app.checkers.CommentEnabledCheck;
import com.odysee.app.views.MediaRelativeLayout;

import javax.net.ssl.SSLParameters;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FileViewFragment extends BaseFragment implements
        MainActivity.BackPressInterceptor,
        ClaimListAdapter.ClaimListItemListener,
        FilePickerListener,
        FetchClaimsListener,
        PIPModeListener,
        ScreenOrientationListener,
        StoragePermissionListener,
        ChannelCreateDialogFragment.ChannelCreateListener,
        MainActivity.PlaylistOverlayDelegate {
    private static final String TAG = "OdyseeFile";
    public static int FILE_CONTEXT_GROUP_ID = 2;
    private static final int RELATED_CONTENT_SIZE = 16;
    @StringRes
    private static final int AUTO_QUALITY_STRING = R.string.auto_quality;
    public static final String CDN_PREFIX = "https://cdn.lbryplayer.xyz";

    private boolean loadingNewClaim;
    private boolean loadingQualityChanged = false;
    private boolean downloadInProgress;
    private boolean downloadRequested;
    private boolean startingFilePicker;
//    private boolean loadFilePending;
    private boolean isPlaying;
//    private boolean resolving;
//    private boolean initialFileLoadDone;
    private Claim fileClaim;
    private Claim collectionClaimItem;
    private String currentPlaylistTitle;
    private String currentUrl;
    private String currentMediaSourceUrl;
    // Stores URL to original (non transcoded) media. Only set in "get" request before download. Do not use otherwise.
    private String currentMediaSourceOriginalUrl;
    private long currentDownloadId;
    private TextView relatedContentTitle;
    private ClaimListAdapter relatedContentAdapter;
    private CommentEnabledCheck commentEnabledCheck;
    private CommentListAdapter commentListAdapter;
    private Player.Listener fileViewPlayerListener;
    private View commentLoadingArea;

    private NestedScrollView scrollView;
    private long elapsedDuration = 0;
    private boolean elapsedPlaybackScheduled;
    private boolean playbackStarted;
    private long startTimeMillis;
    private Handler seekOverlayHandler;

    private boolean storagePermissionRefusedOnce;
    private View buttonPublishSomething;
    private View layoutLoadingState;
    private View layoutNothingAtLocation;
    private View layoutDisplayArea;
    private View layoutResolving;
    private int lastPositionSaved;

    private View tipButton;
    private boolean playlistResolved;

    @Setter
    private List<Claim> playlistClaims = new ArrayList<>();

    private WebView webView;
    private boolean webViewAdded;
    private CreatorSetting currentCreatorSetting;

    private ViewGroup singleCommentRoot;
    private ImageButton expandButton;
    private Comment replyToComment;
    private View containerReplyToComment;
    private View containerCommentForm;
    private TextView textReplyingTo;
    private TextView textReplyToBody;
    private View buttonClearReplyToComment;
    private TextView textNothingAtLocation;

    private TextView likeReactionAmount;
    private TextView dislikeReactionAmount;
    private ImageView likeReactionIcon;
    private ImageView dislikeReactionIcon;
    ScheduledFuture<?> futureReactions;
    Reactions reactions;
    ScheduledFuture<?> futureElapsedPlayback;
    ScheduledFuture<?> scheduledStartPlaying;
    ScheduledFuture<?> scheduledStopPlaying;
    ScheduledExecutorService downloadProgressScheduler;
    private long livestreamStartingMillis = 0; // Stores the time when livestream will start or when it started

    private boolean postingComment;
    private boolean fetchingChannels;
    private View progressLoadingChannels;
    private View progressPostComment;
    private InlineChannelSpinnerAdapter commentChannelSpinnerAdapter;
    private AppCompatSpinner commentChannelSpinner;
    private TextInputEditText inputComment;
    private TextView textCommentLimit;
    private MaterialButton buttonPostComment;
    private MaterialButton buttonCreateChannel;
    private ImageView commentPostAsThumbnail;
    private View commentPostAsNoThumbnail;
    private TextView commentPostAsAlpha;
    private MaterialButton buttonCommentSignedInUserRequired;

    private View layoutActionsArea;
    private View layoutLivestreamChat;
    private View layoutLivestreamChatTip;
    private View layoutCommentsArea;
    private View layoutRelatedContentArea;
    private View dividerRelatedContentArea;
    private View dividerDescriptionArea;

    private View chatHeaderSection;
    private View buttonChatSettings;
    private View buttonChatHeaderClose;
    private RecyclerView chatMessageList;
    private EditText inputChatMessage;
    private View buttonSendChatMessage;
    private View buttonToggleTipArea;
    private View buttonCloseTipArea;
    private TextView labelTipCredits;
    private TextInputEditText inputTipAmount;

    // Flag to be used if we're playing from a playlist
    private boolean alwaysAutoplay;
    private boolean leavingFileView;
    private WebSocketClient webSocketClient;

    // if this is set, scroll to the specific comment on load
    private String commentHash;
    private String claimLivestreamUrl;

    private boolean isLivestream;
    private Map<String, JSONObject> jsonData;

    private Comment.CommenterClickHandler chatMemberClickHandler;
    @Getter
    @Setter
    private String currentCollectionId;

    // Playlist items pseudo-pagination
    int playlistPos = 0, oldPlaylistPos = 0;
    MediaRouteButton mediaRouteButton;
    View playbackQuality;

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commentEnabledCheck = new CommentEnabledCheck();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_file_view, container, false);

        layoutLoadingState = root.findViewById(R.id.file_view_loading_state);
        layoutNothingAtLocation = root.findViewById(R.id.container_nothing_at_location);
        layoutResolving = root.findViewById(R.id.file_view_loading_container);
        layoutDisplayArea = root.findViewById(R.id.file_view_claim_display_area);
        buttonPublishSomething = root.findViewById(R.id.nothing_at_location_publish_button);

        tipButton = root.findViewById(R.id.file_view_action_tip);

        expandButton = root.findViewById(R.id.expand_commentarea_button);
        singleCommentRoot = root.findViewById(R.id.collapsed_comment);
        relatedContentTitle = root.findViewById(R.id.related_or_playlist);

        containerCommentForm = root.findViewById(R.id.container_comment_form);
        containerReplyToComment = root.findViewById(R.id.comment_form_reply_to_container);
        textReplyingTo = root.findViewById(R.id.comment_form_replying_to_text);
        textReplyToBody = root.findViewById(R.id.comment_form_reply_to_body);
        buttonClearReplyToComment = root.findViewById(R.id.comment_form_clear_reply_to);

        commentChannelSpinner = root.findViewById(R.id.comment_form_channel_spinner);
        progressLoadingChannels = root.findViewById(R.id.comment_form_channels_loading);
        progressPostComment = root.findViewById(R.id.comment_form_post_progress);
        inputComment = root.findViewById(R.id.comment_form_body);
        textCommentLimit = root.findViewById(R.id.comment_form_text_limit);
        buttonPostComment = root.findViewById(R.id.comment_form_post);
        buttonCreateChannel = root.findViewById(R.id.create_channel_button);
        commentPostAsThumbnail = root.findViewById(R.id.comment_form_thumbnail);
        commentPostAsNoThumbnail = root.findViewById(R.id.comment_form_no_thumbnail);
        commentPostAsAlpha = root.findViewById(R.id.comment_form_thumbnail_alpha);
        buttonCommentSignedInUserRequired = root.findViewById(R.id.sign_in_user_button);
        textNothingAtLocation = root.findViewById(R.id.nothing_at_location_text);
        commentLoadingArea = root.findViewById(R.id.file_comments_loading);

        likeReactionAmount = root.findViewById(R.id.likes_amount);
        dislikeReactionAmount = root.findViewById(R.id.dislikes_amount);
        likeReactionIcon = root.findViewById(R.id.like_icon);
        dislikeReactionIcon = root.findViewById(R.id.dislike_icon);

        chatHeaderSection = root.findViewById(R.id.file_view_chat_header_section);
        buttonChatHeaderClose = root.findViewById(R.id.chat_header_close_button);
        chatMessageList = root.findViewById(R.id.file_view_live_chat_messages);
        chatMessageList.setLayoutManager(new LinearLayoutManager(getContext()));
        buttonChatSettings = root.findViewById(R.id.file_view_live_chat_settings_button);
        inputChatMessage = root.findViewById(R.id.file_view_live_chat_text_input);
        buttonSendChatMessage = root.findViewById(R.id.file_view_live_chat_send_button);
        buttonToggleTipArea = root.findViewById(R.id.file_view_live_chat_lbc_tip_button);
        buttonCloseTipArea = root.findViewById(R.id.file_view_live_chat_close_tip_area_button);
        labelTipCredits = root.findViewById(R.id.file_view_live_chat_tip_input_currency);
        inputTipAmount = root.findViewById(R.id.file_view_live_chat_tip_input_amount);

        layoutActionsArea = root.findViewById(R.id.file_view_actions_area);
        layoutLivestreamChat = root.findViewById(R.id.file_view_live_chat_area);
        layoutLivestreamChatTip = root.findViewById(R.id.file_view_live_chat_text_input_tip_area);
        layoutCommentsArea = root.findViewById(R.id.file_view_comments_area);
        layoutRelatedContentArea = root.findViewById(R.id.file_view_related_content_area);
        dividerDescriptionArea = root.findViewById(R.id.file_view_divider_description_area);
        dividerRelatedContentArea = root.findViewById(R.id.file_view_divider_related_content_area);
        mediaRouteButton = root.findViewById(R.id.player_media_route_button);
        playbackQuality = root.findViewById(R.id.player_quality);

        initUi(root);
        initLiveChatTippingArea();

        chatMemberClickHandler = new Comment.CommenterClickHandler() {
            @Override
            public void onCommenterClick(String commenter, String commenterClaimId) {
                Context context = getContext();
                LbryUri url = LbryUri.tryParse(String.format("lbry://%s:%s", commenter, commenterClaimId));
                if (context instanceof MainActivity && url != null) {
                    ((MainActivity) context).openChannelUrl(url.toString());
                }
            }
        };

        fileViewPlayerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int playbackState) {
                Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();

                if (playbackState == Player.STATE_READY) {
                    elapsedDuration = currentPlayer.getCurrentPosition();
                    if (!playbackStarted) {
                        logPlay(currentUrl, startTimeMillis);
                        playbackStarted = true;

                        long lastPosition = loadLastPlaybackPosition();
                        if (lastPosition > -1) {
                            currentPlayer.seekTo(lastPosition);
                        }
                    }
                    scheduleElapsedPlayback();
                    setPlayerSurfaceVisibility(View.VISIBLE);

                    if (loadingNewClaim) {
                        setPlaybackSpeedToDefault();
                        setPlayerQualityToDefault();
                        currentPlayer.setPlayWhenReady(true);
                        loadingNewClaim = false;
                    }
                } else if (playbackState == Player.STATE_BUFFERING) {
                    if (!loadingQualityChanged) {
                        Context ctx = getContext();
                        boolean sendBufferingEvents = true;

                        if (ctx != null) {
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
                            sendBufferingEvents = sp.getBoolean(MainActivity.PREFERENCE_KEY_SEND_BUFFERING_EVENTS, true);
                        }

                        if (currentPlayer != null && currentPlayer.getCurrentPosition() > 0 && sendBufferingEvents) {
                            // we only want to log a buffer event after the media has already started playing
                            if (!Helper.isNullOrEmpty(currentMediaSourceUrl)) {
                                long duration = currentPlayer.getDuration();
                                long position = currentPlayer.getCurrentPosition();
                                String userIdHash = Lbryio.currentUser != null ? String.valueOf(Lbryio.currentUser.getId()) : "0";
                                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;

                                BufferEventTask bufferEvent = new BufferEventTask(actualClaim.getPermanentUrl(), duration, position, 1, userIdHash);
                                bufferEvent.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                            }
                        }
                    }

                    loadingQualityChanged = false;

                    if (isLivestream) {
                        Timeline.Window window = currentPlayer.getCurrentTimeline()
                                .getWindow(currentPlayer.getCurrentMediaItemIndex(), new Timeline.Window());
                        long currentPosition = currentPlayer.getCurrentPosition();
                        long defaultPosition = window.getDefaultPositionMs();
                        if (currentPosition >= defaultPosition) {
                            setPlaybackSpeed(currentPlayer, 100);
                        }
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    playNextItemInPlaylist();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlayng) {
                isPlaying = isPlayng;
            }
        };

        scrollView = root.findViewById(R.id.file_view_scroll_view);
        return root;
    }

    private void playNextItemInPlaylist() {
        if (playlistClaims.size() > 0 && collectionClaimItem != null) {
            int collectionClaimIndex = playlistClaims.indexOf(collectionClaimItem);
            if (collectionClaimIndex > -1) {
                int nextIndex = collectionClaimIndex + 1;
                if (nextIndex < playlistClaims.size() - 1) {
                    playClaimFromCollection(playlistClaims.get(nextIndex), nextIndex);
                } else {
                    // looks like we're at the last item, check if loop is enabled
                    Context context = getContext();
                    if (context instanceof MainActivity && ((MainActivity) context).isPlaylistLoopEnabled()) {
                        playFirstItemInCollection();
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Context context = getContext();

        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.hideToolbar();
            activity.setBackPressInterceptor(this);
            activity.addFilePickerListener(this);
            activity.addFetchClaimsListener(this);
            activity.addPIPModeListener(this);
            activity.addScreenOrientationListener(this);
            if (!MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
                activity.addStoragePermissionListener(this);
            }
        }
    }

    private void handlePlayCollection(Map<String, Object> params) {
        OdyseeCollection collection = (OdyseeCollection) params.get("collection");
        playlistClaims = new ArrayList<>(collection.getClaims());
        playlistResolved = true;
        currentCollectionId = collection.getId();
        currentPlaylistTitle = collection.getName();

        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).setCurrentPlaylist(collection);
        }

        if (playlistClaims.size() > 0) {
            if (params.containsKey("item") && params.containsKey("itemIndex")) {
                int index = (int) params.get("itemIndex");
                playClaimFromCollection(playlistClaims.get(index), index);
            } else {
                playClaimFromCollection(playlistClaims.get(0), 0);
            }
        }
    }

    private void checkParams() {
        boolean updateRequired = false;
        Context context = getContext();
//        claim.setClaimId(FileViewFragmentArgs.fromBundle(getArguments()).getClaimId());
        try {
            Map<String, Object> params = getParams();
            Claim newClaim = null;
            String newUrl = null;
            if (params != null) {
                if (params.containsKey("collection")) {
                    handlePlayCollection(params);
                    return;
                }

                if (params.containsKey("claim")) {
                    newClaim = (Claim) params.get("claim");
                    // Only update fragment if new claim is different from currently being played
                    if (newClaim != null && !newClaim.equals(this.fileClaim)) {
                        updateRequired = true;
                    }
                }

                if (params.containsKey("url")) {
                    Object urlParam = params.get("url");
                    if (urlParam != null) {
                        LbryUri newLbryUri = LbryUri.tryParse(urlParam.toString());
                        if (newLbryUri != null) {
                            newUrl = newLbryUri.toString();
                            String qs = newLbryUri.getQueryString();
                            if (!Helper.isNullOrEmpty(qs)) {
                                String[] qsPairs = qs.split("&");
                                for (String pair : qsPairs) {
                                    String[] parts = pair.split("=");
                                    if (parts.length < 2) {
                                        continue;
                                    }
                                    if ("comment_hash".equalsIgnoreCase(parts[0])) {
                                        commentHash = parts[1];
                                        break;
                                    }
                                }
                            }

                            if (fileClaim == null || !newUrl.equalsIgnoreCase(currentUrl)) {
                                updateRequired = true;
                            }
                        }
                    }
                }
                if (params.containsKey("livestreamUrl") && params.get("livestreamUrl") != null) {
                    claimLivestreamUrl = (String) params.get("livestreamUrl");
                }
            } else if (currentUrl != null) {
                updateRequired = true;
            } else if (context instanceof MainActivity) {
                ((MainActivity) context).onBackPressed();
            }

            boolean invalidRepost = false;
            if (updateRequired) {
                resetViewCount();
                resetFee();

                // This is required to recycle current fragment with new claim from related content
                fileClaim = null;

                if (newClaim != null) {
                    fileClaim = newClaim;
                }

                if (fileClaim == null && !Helper.isNullOrEmpty(newUrl)) {
                    // check if the claim is already cached
                    currentUrl = newUrl;
                    ClaimCacheKey key = new ClaimCacheKey();
                    key.setUrl(currentUrl);
                    onNewClaim(currentUrl);
                    if (Lbry.claimCache.containsKey(key)) {
                        fileClaim = Lbry.claimCache.get(key);
                    }
                }
                if (fileClaim != null && Claim.TYPE_REPOST.equalsIgnoreCase(fileClaim.getValueType())) {
                    fileClaim = fileClaim.getRepostedClaim();
                    if (fileClaim == null || Helper.isNullOrEmpty(fileClaim.getClaimId())) {
                        // Invalid repost, probably
                        invalidRepost = true;
                        renderNothingAtLocation();
                    } else if (fileClaim.getName().startsWith("@")) {
                        // this is a reposted channel, so launch the channel url
                        if (context instanceof MainActivity) {
                            MainActivity activity = (MainActivity) context;
                            //activity.onBackPressed(); // remove the reposted url page from the back stack
                            activity.getSupportFragmentManager().popBackStack();
                            activity.openChannelUrl(!Helper.isNullOrEmpty(fileClaim.getShortUrl()) ? fileClaim.getShortUrl() : fileClaim.getPermanentUrl());
                        }
                        return;
                    }
                }
                if (fileClaim == null) {
                    resolveUrl(currentUrl);
                } else {
                    Lbry.addClaimToCache(fileClaim);
                    loadingNewClaim = true;
                }
            } else {
                checkAndResetNowPlayingClaim();
            }

            if (!Helper.isNullOrEmpty(currentUrl)) {
                Helper.saveUrlHistory(currentUrl, fileClaim != null ? fileClaim.getTitle() : null, UrlSuggestion.TYPE_FILE);
            }

            if (fileClaim != null && !invalidRepost) {
                if (Claim.TYPE_COLLECTION.equalsIgnoreCase(fileClaim.getValueType()) &&
                        fileClaim.getClaimIds() != null && fileClaim.getClaimIds().size() > 0) {
                    currentPlaylistTitle = fileClaim.getTitleOrName();
                    resolvePlaylistClaimsAndPlayFirst();
                    return;
                }

                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (!Claim.TYPE_COLLECTION.equalsIgnoreCase(actualClaim.getType())) {
                    // We don't want to save actual collections to the view history
                    Helper.saveViewHistory(actualClaim.getUnifiedUrl(), actualClaim);
                }

                CustomBlockRule.CustomBlockStatus status = null;
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    status = Helper.getCustomBlockedStatus(
                            actualClaim.getClaimId(),
                            activity.getCustomBlockingRulesMap(),
                            activity.getOdyseeLocale(),
                            getContext());
                }

                if ((status != null && status.isBlocked()) || Helper.isClaimBlocked(actualClaim)) {
                    renderClaimBlocked(status.getMessage());
                } else {
                    checkAndLoadChannelSettings();
                    checkAndLoadRelatedContent();
                    checkAndLoadComments();

                    renderClaim();
                    if (actualClaim.getFile() == null) {
                        loadFile();
                    } else {
//                    initialFileLoadDone = true;
                    }
                }
            }
        } catch (Exception ex){
            android.util.Log.e(TAG, ex.getMessage(), ex);
        }
    }

    private void renderPublisherNotBroadcasting(Claim claimToRender) {
        View root = getView();
        Context context = getContext();
        if (root != null && context != null) {
            root.findViewById(R.id.file_view_livestream_not_live).setVisibility(View.VISIBLE);
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
            TextView userNotStreaming = root.findViewById(R.id.user_not_streaming);
            userNotStreaming.setText(context.getString(R.string.user_not_live_yet, claimToRender.getPublisherName()));
            userNotStreaming.setVisibility(View.VISIBLE);
            ImageView thumbnailView = root.findViewById(R.id.file_view_livestream_thumbnail);
            Glide.with(context.getApplicationContext()).
                    asBitmap().
                    load(claimToRender.getThumbnailUrl()).
                    into(thumbnailView);
        }
        updatePublishTime((Claim.StreamMetadata)claimToRender.getValue(), claimToRender);
    }

    private void renderPublisherBroadcasting() {
        View root = getView();
        Context context = getContext();
        if (root != null && context != null) {
            root.findViewById(R.id.file_view_livestream_not_live).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.VISIBLE);
            TextView userNotStreaming = root.findViewById(R.id.user_not_streaming);
            userNotStreaming.setVisibility(View.GONE);
        }
        updatePublishTime(null, null);
    }

    private void renderNothingAtLocation() {
        Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.VISIBLE);
        Helper.setViewVisibility(buttonPublishSomething, View.VISIBLE);
        Helper.setViewVisibility(layoutResolving, View.GONE);
        Helper.setViewVisibility(layoutDisplayArea, View.INVISIBLE);
        if (textNothingAtLocation != null) {
            textNothingAtLocation.setText(R.string.nothing_at_this_location);
        }
    }

    private void renderClaimBlocked(String blockedMessage) {
        Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.VISIBLE);
        Helper.setViewVisibility(buttonPublishSomething, View.INVISIBLE);
        Helper.setViewVisibility(layoutResolving, View.GONE);
        Helper.setViewVisibility(layoutDisplayArea, View.INVISIBLE);
        if (textNothingAtLocation != null) {
            textNothingAtLocation.setMovementMethod(LinkMovementMethod.getInstance());
            textNothingAtLocation.setText(
                    !Helper.isNullOrEmpty(blockedMessage) ? blockedMessage :
                    HtmlCompat.fromHtml(getString(R.string.dmca_complaint_blocked), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    private void updatePublishTime(@Nullable Claim.StreamMetadata metadata, @Nullable Claim claim) {
        View root = getView();
        if (root != null) {
            long publishTime = 0;
            if (metadata != null && claim != null) {
                publishTime = metadata.getReleaseTime() > 0 ? metadata.getReleaseTime() * 1000 : claim.getTimestamp() * 1000;
            } else if (claim == null && livestreamStartingMillis != 0){
                publishTime = livestreamStartingMillis;
            }

            ((TextView) root.findViewById(R.id.file_view_publish_time)).setText(DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.MEDIUM).format(Instant.ofEpochMilli(publishTime).atZone(ZoneId.systemDefault())));
        }
    }

    @Override
    public void onViewCreated(@NotNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        if (savedInstanceState != null) {
            currentUrl = savedInstanceState.getString("url");
        }
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("url", currentUrl);
    }

    private void initWebView(View root) {
        Context ctx = getContext();
        if (ctx != null) {
            if (webView == null) {
                webView = new WebView(ctx);
                webView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                webView.setWebViewClient(new LbryWebViewClient(ctx));
                WebSettings webSettings = webView.getSettings();
                webSettings.setAllowFileAccess(true);
                webSettings.setJavaScriptEnabled(false);
            }

            if (!webViewAdded && root != null) {
                ((RelativeLayout) root.findViewById(R.id.file_view_webview_container)).addView(webView);
                webViewAdded = true;
            }
        }
    }

    private void applyThemeToWebView() {
        Context context = getContext();
        if (context instanceof MainActivity && webView != null && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            int defaultNight = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            MainActivity activity = (MainActivity) context;
            String darkModeAppSetting = ((OdyseeApp) activity.getApplication()).getDarkModeAppSetting();
            WebSettingsCompat.setForceDark(webView.getSettings(), (darkModeAppSetting.equals(MainActivity.APP_SETTING_DARK_MODE_NIGHT) || (darkModeAppSetting.equals(MainActivity.APP_SETTING_DARK_MODE_SYSTEM) && defaultNight == Configuration.UI_MODE_NIGHT_YES)) ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
        }
    }

    private void logUrlEvent(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_OPEN_FILE_PAGE, bundle);
    }

    private void checkAndResetNowPlayingClaim() {
        checkAndResetNowPlayingClaim(false);
    }

    private void checkAndResetNowPlayingClaim(boolean fromPlaylist) {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (MainActivity.nowPlayingClaim != null
                && actualClaim != null &&
                !MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(actualClaim.getClaimId())) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                // only clear the playlist if this didn't get played from an active playlist
                boolean shouldClearPlaylist = !fromPlaylist;
                activity.clearNowPlayingClaim(shouldClearPlaylist);
                if (actualClaim != null && !actualClaim.isPlayable()) {
                    MainActivity.stopExoplayer();
                }
            }
            setPlayerSurfaceVisibility(View.INVISIBLE);
        }
    }

    private void onNewClaim(String url) {
        loadingNewClaim = true;
//        initialFileLoadDone = false;
        playbackStarted = false;
        currentUrl = url;
        logUrlEvent(url);
        resetViewCount();
        resetFee();

        View root = getView();
        if (root != null) {
            if (relatedContentAdapter != null) {
                relatedContentAdapter.clearItems();
            }
            if (commentListAdapter != null) {
                commentListAdapter.clearItems();
            }
            ((RecyclerView) root.findViewById(R.id.file_view_related_content_list)).setAdapter(null);
            ((RecyclerView) root.findViewById(R.id.file_view_comments_list)).setAdapter(null);
        }
    }

    private String getStreamingUrl() {
        if (claimLivestreamUrl != null) {
            return claimLivestreamUrl;
        } else {
            return buildLbryTvStreamingUrl();
        }
    }

    // TODO: Deprecate
    private String buildLbryTvStreamingUrl() {
        return String.format("%s/content/claims/%s/%s/stream", CDN_PREFIX, fileClaim.getName(), fileClaim.getClaimId());
    }

    private void loadFile() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        String claimId = actualClaim.getClaimId();
        FileListTask task = new FileListTask(claimId, null, new FileListTask.FileListResultHandler() {
            @Override
            public void onSuccess(List<LbryFile> files, boolean hasReachedEnd) {
                if (files.size() > 0) {
                    actualClaim.setFile(files.get(0));
                    if (!actualClaim.isPlayable() && !actualClaim.isViewable()) {
                        showUnsupportedView();
                    }
                } else {
                    if (!actualClaim.isPlayable() && !actualClaim.isViewable()) {
                        restoreMainActionButton();
                    }
                }

//                initialFileLoadDone = true;
            }

            @Override
            public void onError(Exception error) {
//                initialFileLoadDone = true;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
/*
    public void openClaimUrl(String url) {
        resetViewCount();
        resetFee();
        currentUrl = url;

        ClaimCacheKey key = new ClaimCacheKey();
        key.setUrl(currentUrl);
        Claim oldClaim = claim;
        claim = null;
        if (Lbry.claimCache.containsKey(key)) {
            claim = Lbry.claimCache.get(key);
            if (oldClaim != null && oldClaim.getClaimId().equalsIgnoreCase(claim.getClaimId())) {
                // same claim
                return;
            }
        } else {
            resolveUrl(currentUrl);
        }

        resetMedia();
        onNewClaim(currentUrl);
        Helper.setWunderbarValue(currentUrl, getContext());

        if (claim != null) {
            Helper.saveViewHistory(url, claim);
            if (Helper.isClaimBlocked(claim)) {
                renderClaimBlocked();
            } else {
                checkAndLoadRelatedContent();
                checkAndLoadComments();
                renderClaim();
            }
        }
    }

    public void resetMedia() {
        View root = getView();
        if (root != null) {
            PlayerView view = root.findViewById(R.id.file_view_exoplayer_view);
            view.setShutterBackgroundColor(Color.BLACK);
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
        }
        if (MainActivity.appPlayer != null) {
            MainActivity.appPlayer.stop();
        }
        resetPlayer();
    }
*/

    @Override
    public void onResume() {
        super.onResume();

        Context context = getContext();

        // Skip init if resuming from file picker
        if (startingFilePicker) {
            startingFilePicker = MainActivity.startingFilePickerActivity;
            setPlayerForPlayerView();
            if (context instanceof MainActivity) {
                ((MainActivity) context).hideGlobalNowPlaying();
            }
            return;
        }

        checkParams();
        leavingFileView = false;

        if (initialChatLoaded) {
            checkWebSocketClient();
        }

        Helper.setWunderbarValue(currentUrl, context);
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "File", "File");
            activity.updateCurrentDisplayFragment(this);
            activity.setPlaylistOverlayDelegate(this);
            if (actualClaim != null && actualClaim.isPlayable() && activity.isInFullscreenMode()) {
                enableFullScreenMode();
            }
            activity.findViewById(R.id.appbar).setFitsSystemWindows(false);
            activity.checkIfPlaylistOverlayShouldDisplay();
            activity.refreshChannelCreationRequired(getView());
            activity.updateMiniPlayerMargins(false);

            OdyseeCollection nowPlayingCollection = activity.getNowPlayingQueuePlaylist();
            if (nowPlayingCollection != null && (!nowPlayingCollection.getId().equalsIgnoreCase(currentCollectionId) ||
                    playlistClaims == null || nowPlayingCollection.getClaims().size() != playlistClaims.size())) {
                playlistClaims = new ArrayList<>(nowPlayingCollection.getClaims());
                playlistResolved = true;
                currentCollectionId = nowPlayingCollection.getId();
                currentPlaylistTitle = nowPlayingCollection.getName();
            }
        }

        if (MainActivity.playerManager != null) {
            if (MainActivity.playerReassigned) {
                setPlayerForPlayerView();
                MainActivity.playerReassigned = false;
            }

            View root = getView();
            if (root != null) {
                StyledPlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
                if (playerView.getPlayer() == null) {
                    playerView.setPlayer(MainActivity.playerManager.getCurrentPlayer());
                }
                updateQualityView(root);
            }

            loadAndScheduleDurations();
        }

        checkOwnClaim();
        fetchChannels();
        applyFilterForMutedChannels(Lbryio.mutedChannels);
    }

    @Override
    public void onPause() {
        if (MainActivity.playerManager != null) {
            MainActivity.nowPlayingSource = MainActivity.SOURCE_NOW_PLAYING_FILE;
        }
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.hidePlaylistOverlay();
            activity.updateMiniPlayerMargins(true);
            activity.setPlaylistOverlayDelegate(null);
        }
        leavingFileView = true;
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeFetchClaimsListener(this);
            activity.removePIPModeListener(this);
            activity.removeScreenOrientationListener(this);
            activity.removeStoragePermissionListener(this);
            activity.showAppBar();
            activity.checkNowPlaying();

            activity.resetCurrentDisplayFragment();

            startingFilePicker = MainActivity.startingFilePickerActivity;
            if (!MainActivity.startingFilePickerActivity) {
                activity.removeFilePickerListener(this);
            }
        }

        closeWebView();

        // Tasks on the scheduled executor needs to be really terminated to avoid
        // crashes if user presses back after going to a related content from here
        purgeScheduledTasks();
    }

    /**
     * Cancels scheduled futures
     */
    private void purgeScheduledTasks() {
        if (futureReactions != null) {
            futureReactions.cancel(true);
        }
        if (futureElapsedPlayback != null) {
            futureElapsedPlayback.cancel(true);
            elapsedPlaybackScheduled = false;
        }
        if (scheduledStartPlaying != null) {
            scheduledStartPlaying.cancel(true);
        }
        if (scheduledStopPlaying != null) {
            scheduledStopPlaying.cancel(true);
        }

        if (futureReactions != null && futureReactions.isDone()) {
            futureReactions = null;
        }
        if (futureElapsedPlayback != null && futureElapsedPlayback.isDone()) {
            futureElapsedPlayback = null;
        }
        if (scheduledStartPlaying != null && scheduledStartPlaying.isDone()) {
            scheduledStartPlaying = null;
        }
        if (scheduledStopPlaying != null && scheduledStopPlaying.isDone()) {
            scheduledStopPlaying = null;
        }
    }

    private void closeWebView() {
        if (webView != null) {
            webView.removeAllViews();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        webViewAdded = false;
    }

    private void setPlayerForPlayerView() {
        View root = getView();
        if (root != null) {
            StyledPlayerView view = root.findViewById(R.id.file_view_exoplayer_view);
            view.setVisibility(View.VISIBLE);
            Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();
            view.setPlayer(null);
            view.setPlayer(currentPlayer);
            view.setControllerHideOnTouch(currentPlayer instanceof ExoPlayer);
            view.setControllerShowTimeoutMs(currentPlayer instanceof ExoPlayer
                    ? StyledPlayerControlView.DEFAULT_SHOW_TIMEOUT_MS : 0);
            // This hack shouldn't be needed (the code without it works in the ExoPlayer demo)
            setPlayerSurfaceVisibility(currentPlayer instanceof ExoPlayer ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void setPlayerSurfaceVisibility(int visibility) {
        View root = getView();
        if (root != null) {
            StyledPlayerView view = root.findViewById(R.id.file_view_exoplayer_view);
            View surfaceView = view.getVideoSurfaceView();
            if (surfaceView != null) {
                surfaceView.setVisibility(visibility);
            }
        }
    }

    private final View.OnClickListener bellIconListener = new View.OnClickListener()  {
        @Override
        public void onClick(View view) {
            Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
            // View is not displayed when user is not signed in, so no need to check for it
            if (actualClaim != null && actualClaim.getSigningChannel() != null) {
                Claim publisher = actualClaim.getSigningChannel();
                boolean isNotificationsDisabled = Lbryio.isNotificationsDisabled(publisher);
                final Subscription subscription = Subscription.fromClaim(publisher);
                subscription.setNotificationsDisabled(!isNotificationsDisabled);
                view.setEnabled(false);
                Context context = getContext();
                final View rootView = getView();
                new ChannelSubscribeTask(context, publisher.getClaimId(), subscription, false, new ChannelSubscribeTask.ChannelSubscribeHandler() {
                    @Override
                    public void onSuccess() {
                        view.setEnabled(true);
                        Lbryio.updateSubscriptionNotificationsDisabled(subscription);
                        checkIsFollowing();
                        showMessage(subscription.isNotificationsDisabled() ?
                                R.string.receive_no_notifications : R.string.receive_all_notifications);

                        Context context = getContext();
                        if (context != null) {
                            context.sendBroadcast(new Intent(MainActivity.ACTION_SAVE_SHARED_USER_STATE));
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        view.setEnabled(true);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    };

    private final View.OnClickListener followUnfollowListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            // TODO Extract this code to MainActivity and update views state from there for any currently visible fragments
            MainActivity activity = (MainActivity) getActivity();

            if (activity != null && activity.isSignedIn()) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null && actualClaim.getSigningChannel() != null) {
                    Claim publisher = actualClaim.getSigningChannel();
                    boolean isFollowing = Lbryio.isFollowing(publisher);
                    if (isFollowing) {
                        // show unfollow confirmation
                        Context context = getContext();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context).
                                setTitle(R.string.confirm_unfollow).
                                setMessage(R.string.confirm_unfollow_message)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        doFollowUnfollow(true, view);
                                    }
                                }).setNegativeButton(R.string.no, null);
                        builder.show();
                    } else {
                        doFollowUnfollow(false, view);
                    }
                }
            } else {
                if (activity != null) {
                    activity.simpleSignIn(0);
                }
            }
        }
    };

    private void doFollowUnfollow(boolean isFollowing, View view) {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null && actualClaim.getSigningChannel() != null) {
            Claim publisher = actualClaim.getSigningChannel();
            Subscription subscription = Subscription.fromClaim(publisher);
            view.setEnabled(false);
            Context context = getContext();
            new ChannelSubscribeTask(context, publisher.getClaimId(), subscription, isFollowing, new ChannelSubscribeTask.ChannelSubscribeHandler() {
                @Override
                public void onSuccess() {
                    if (isFollowing) {
                        Lbryio.removeSubscription(subscription);
                        Lbryio.removeCachedResolvedSubscription(publisher);
                    } else {
                        Lbryio.addSubscription(subscription);
                        Lbryio.addCachedResolvedSubscription(publisher);
                    }
                    view.setEnabled(true);
                    checkIsFollowing();
                    FollowingFragment.resetClaimSearchContent = true;

                    // Save shared user state
                    if (context != null) {
                        context.sendBroadcast(new Intent(MainActivity.ACTION_SAVE_SHARED_USER_STATE));
                    }
                }

                @Override
                public void onError(Exception exception) {
                    view.setEnabled(true);
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void resolveUrl(String url) {
//        resolving = true;
        Helper.setViewVisibility(layoutDisplayArea, View.INVISIBLE);
        Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.GONE);
        ResolveTask task = new ResolveTask(url, Lbry.API_CONNECTION_STRING, layoutResolving, new ResolveResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                if (!claims.isEmpty() && !Helper.isNullOrEmpty(claims.get(0).getClaimId())) {
                    fileClaim = claims.get(0);
                    if (Claim.TYPE_REPOST.equalsIgnoreCase(fileClaim.getValueType())) {
                        fileClaim = fileClaim.getRepostedClaim();
                        // cache the reposted claim too for subsequent loads
                        Lbry.addClaimToCache(fileClaim);
                        if (fileClaim.getName().startsWith("@")) {
                            // this is a reposted channel, so finish this activity and launch the channel url
                            Context context = getContext();
                            if (context instanceof  MainActivity) {
                                MainActivity activity = (MainActivity) context;
                                activity.getSupportFragmentManager().popBackStack();
                                activity.openChannelUrl(!Helper.isNullOrEmpty(fileClaim.getShortUrl()) ? fileClaim.getShortUrl() : fileClaim.getPermanentUrl());
                            }
                            return;
                        }
                    } else {
                        Lbry.addClaimToCache(fileClaim);
                    }

                    if (Claim.TYPE_COLLECTION.equalsIgnoreCase(fileClaim.getValueType()) &&  fileClaim.getClaimIds() != null && fileClaim.getClaimIds().size() > 0) {
                        collectionClaimItem = null;
                    }

                    Helper.saveUrlHistory(url, fileClaim.getTitle(), UrlSuggestion.TYPE_FILE);

                    // do not save collections to view history
                    if (!Claim.TYPE_COLLECTION.equalsIgnoreCase(fileClaim.getType())) {
                        // also save view history
                        Helper.saveViewHistory(fileClaim.getUnifiedUrl(), fileClaim);
                    }

                    CustomBlockRule.CustomBlockStatus status = null;
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        MainActivity activity = (MainActivity) context;
                        status = Helper.getCustomBlockedStatus(
                                fileClaim.getClaimId(),
                                activity.getCustomBlockingRulesMap(),
                                activity.getOdyseeLocale(),
                                getContext());
                    }

                    if ((status != null && status.isBlocked()) || Helper.isClaimBlocked(fileClaim)) {
                        renderClaimBlocked(status.getMessage());
                    } else {
                        loadFile();
                        checkAndLoadChannelSettings();
                        checkAndLoadRelatedContent();
                        checkAndLoadComments();
                        renderClaim();
                    }
                } else {
                    // render nothing at location
                    renderNothingAtLocation();
                }
            }

            @Override
            public void onError(Exception error) {
//                resolving = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressWarnings("ClickableViewAccessibility")
    private void initUi(View root) {
        buttonPublishSomething.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (!Helper.isNullOrEmpty(currentUrl) && context instanceof MainActivity) {
                    LbryUri uri = LbryUri.tryParse(currentUrl);
                    if (uri != null) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("suggestedUrl", uri.getStreamName());
//                        ((MainActivity) context).openFragment(PublishFragment.class, true, NavMenuItem.ID_ITEM_NEW_PUBLISH, params);
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_title_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView descIndicator = root.findViewById(R.id.file_view_desc_toggle_arrow);
                View descriptionArea = root.findViewById(R.id.file_view_description_area);

                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                boolean hasDescription = actualClaim != null && !Helper.isNullOrEmpty(actualClaim.getDescription());
                boolean hasTags = actualClaim != null && actualClaim.getTags() != null && actualClaim.getTags().size() > 0;

                if (descriptionArea.getVisibility() != View.VISIBLE) {
                    if (hasDescription || hasTags) {
                        descriptionArea.setVisibility(View.VISIBLE);
                    }
                    descIndicator.setImageResource(R.drawable.ic_arrow_dropup);
                } else {
                    descriptionArea.setVisibility(View.GONE);
                    descIndicator.setImageResource(R.drawable.ic_arrow_dropdown);
                }
            }
        });

        root.findViewById(R.id.file_view_action_like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountManager am = AccountManager.get(root.getContext());
                Account odyseeAccount = Helper.getOdyseeAccount(am.getAccounts());
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (odyseeAccount != null) {
                    react(actualClaim, true);
                } else {
                    MainActivity a = (MainActivity) getActivity();
                    if (a != null) {
                        a.driveUserSignIn();
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_dislike).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccountManager am = AccountManager.get(root.getContext());
                Account odyseeAccount = Helper.getOdyseeAccount(am.getAccounts());
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (odyseeAccount != null) {
                    react(actualClaim, false);
                } else {
                    MainActivity a = (MainActivity) getActivity();
                    if (a != null) {
                        a.driveUserSignIn();
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();

                if (activity != null && activity.isSignedIn()) {
                    Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                    if (actualClaim != null) {
                        CreateSupportDialogFragment dialog = CreateSupportDialogFragment.newInstance(actualClaim, (amount, isTip) -> {
                            double sentAmount = amount.doubleValue();
                            String message = getResources().getQuantityString(
                                    isTip ? R.plurals.you_sent_a_tip : R.plurals.you_sent_a_support, sentAmount == 1.0 ? 1 : 2,
                                    new DecimalFormat("#,###.##").format(sentAmount));
                            showMessage(message);
                        });
                        Context context = getContext();
                        if (context instanceof MainActivity) {
                            dialog.show(((MainActivity) context).getSupportFragmentManager(), CreateSupportDialogFragment.TAG);
                        }
                    }
                } else {
                    if (activity != null) {
                        activity.simpleSignIn(0);
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();

                if (activity != null) {
                    Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                    if (actualClaim != null) {
                        activity.handleAddUrlToList(actualClaim.getPermanentUrl(), null);
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null) {
                    try {
                        String shareUrl = LbryUri.parse(
                                !Helper.isNullOrEmpty(actualClaim.getCanonicalUrl()) ? actualClaim.getCanonicalUrl() :
                                        (!Helper.isNullOrEmpty(actualClaim.getShortUrl()) ? actualClaim.getShortUrl() : actualClaim.getPermanentUrl())).toOdyseeString();
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);

                        MainActivity.startingShareActivity = true;
                        Intent shareUrlIntent = Intent.createChooser(shareIntent, getString(R.string.share_lbry_content));
                        shareUrlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(shareUrlIntent);
                    } catch (LbryUriException ex) {
                        // pass
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_repost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null) {
                    RepostClaimDialogFragment dialog = RepostClaimDialogFragment.newInstance(actualClaim, claim -> {
                        showMessage(R.string.content_successfully_reposted);
                    });
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        dialog.show(((MainActivity) context).getSupportFragmentManager(), RepostClaimDialogFragment.TAG);
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null && context instanceof MainActivity) {
                    ((MainActivity) context).openPublishForm(actualClaim);
                }
            }
        });

        root.findViewById(R.id.file_view_action_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                        setTitle(R.string.delete_content).
                        setMessage(R.string.confirm_delete_content_message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteCurrentClaim();
                            }
                        }).setNegativeButton(R.string.no, null);
                    builder.show();
                }
            }
        });

        root.findViewById(R.id.file_view_action_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null) {
                    if (downloadInProgress) {
                        abortDownload();
                    } else {
                        checkStoragePermissionAndStartDownload();
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null) {
                    Context context = getContext();
                    CustomTabColorSchemeParams.Builder ctcspb = new CustomTabColorSchemeParams.Builder();
                    ctcspb.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    CustomTabColorSchemeParams ctcsp = ctcspb.build();

                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setDefaultColorSchemeParams(ctcsp);
                    CustomTabsIntent intent = builder.build();
                    intent.launchUrl(context, Uri.parse(String.format("https://odysee.com/$/report_content?claimId=%s", actualClaim.getClaimId())));
                }
            }
        });

        MediaRelativeLayout mediaContainer = root.findViewById(R.id.file_view_media_container);
        StyledPlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);

        Context context = getContext();

        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.playerMediaRouteButton = mediaRouteButton;

            if (activity.mediaRouteButtonVisible) {
                activity.playerMediaRouteButton.setVisibility(View.VISIBLE);
            }

            activity.getCastHelper().setUpCastButton(activity.playerMediaRouteButton);
        }

        playbackQuality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openContextMenu(playbackQuality);
                }
            }
        });

        playbackQuality.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                if (MainActivity.playerManager != null) {
                    Helper.buildQualityMenu(contextMenu, MainActivity.playerManager.getCurrentPlayer(), MainActivity.videoIsTranscoded);
                }
            }
        });

        playerView.setControllerVisibilityListener(new StyledPlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                if (visibility == View.VISIBLE) {
                    ((ViewGroup)playbackQuality.getParent()).setVisibility(View.VISIBLE);
                    updateQualityView(root);
               } else {
                    ((ViewGroup)playbackQuality.getParent()).setVisibility(View.GONE);
               }
             }
        });

        playerView.setFullscreenButtonClickListener(new StyledPlayerView.FullscreenButtonClickListener() {
            @Override
            public void onFullscreenButtonClick(boolean isFullScreen) {
                if (!isFullScreen) {
                    disableFullScreenMode();
                    smoothScrollToLastChatMessage(); // If there are no chat messages or claim is not for a livestream, this will do nothing
                } else {
                    enableFullScreenMode();
                }
            }
        });
        root.findViewById(R.id.file_view_publisher_info_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                if (actualClaim != null && actualClaim.getSigningChannel() != null) {
                    removeNotificationAsSource();

                    Claim publisher = actualClaim.getSigningChannel();
                    Context context = getContext();
                    if (context instanceof  MainActivity) {
                        ((MainActivity) context).openChannelClaim(publisher);
                    }
                }
            }
        });

        View buttonFollow = root.findViewById(R.id.file_view_icon_follow);
        View buttonUnfollow = root.findViewById(R.id.file_view_icon_unfollow);
        View buttonBell = root.findViewById(R.id.file_view_icon_bell);
        buttonFollow.setOnClickListener(followUnfollowListener);
        buttonUnfollow.setOnClickListener(followUnfollowListener);
        buttonBell.setOnClickListener(bellIconListener);

        commentChannelSpinnerAdapter = new InlineChannelSpinnerAdapter(getContext(), R.layout.spinner_item_channel, new ArrayList<>());
        commentChannelSpinnerAdapter.addPlaceholder(false);

        initCommentForm(root);
        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prevents crash for when comment list isn't loaded yet but user tries to expand.
                if (commentListAdapter != null) {
                    switchCommentListVisibility(commentListAdapter.isCollapsed());
                    commentListAdapter.switchExpandedStateUI();
                }
            }
        });

        singleCommentRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandButton.performClick();
            }
        });

        buttonChatHeaderClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context != null) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    sp.edit().putBoolean("com.odysee.app.force_display_chat_messages", false).commit();
                }

                chatMessageListAdapter.setDisplayMessages(false);
                root.findViewById(R.id.chat_header_close_button).setVisibility(View.GONE);
                buttonChatSettings.setVisibility(View.VISIBLE);

                displayChatMessages(false, true);
            }
        });

        buttonChatSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    // Linter warns this, but for dialogs and popup windows it is ok to pass 'null' as root
                    @SuppressLint("InflateParams") View popupView = layoutInflater.inflate(R.layout.popup_chat_settings,null);
                    int scaledValue = activity.getScaledValue(240);
                    PopupWindow popupWindow = new PopupWindow(popupView, scaledValue, WindowManager.LayoutParams.WRAP_CONTENT);
                    popupWindow.setFocusable(true);
                    popupWindow.showAsDropDown(buttonChatSettings);

                    // FIXME When more options are added, this options button will be displayed all time, so the visibility change button will need to be always displayed and its functionality be accordingly changed depending on the current state
                    popupView.findViewById(R.id.button_change_chat_visibility).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context context = getContext();
                            if (context != null) {
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                                sp.edit().putBoolean("com.odysee.app.force_display_chat_messages", true).commit();
                            }
                            popupWindow.dismiss();
                            displayChatMessages(true, true);
                            buttonChatHeaderClose.setVisibility(View.VISIBLE);
                            buttonChatSettings.setVisibility(View.GONE);
                            chatMessageListAdapter.setDisplayMessages(true);
                        }
                    });
                }
            }
        });

        buttonSendChatMessage.setEnabled(false);
        buttonSendChatMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                String chatMessage = Helper.getValue(inputChatMessage.getText());

                if (fetchingChannels) {
                    // still fetching channels, don't do anything yet
                    showError(getString(R.string.wait_while_channels_loaded));
                    return;
                }

                if (Lbry.ownChannels.size() == 0) {
                    showError(getString(R.string.create_channel_for_chat));
                    return;
                }

                if (!Helper.isNullOrEmpty(chatMessage) && Lbry.ownChannels.size() > 0) {
                    // get channel to send chat messages as from set default channel
                    String defaultChannelName = Helper.getDefaultChannelName(context);
                    List<Claim> defaultChannel = Lbry.ownChannels
                            .stream()
                            .filter(c -> c != null && c.getName().equalsIgnoreCase(defaultChannelName))
                            .collect(Collectors.toList());
                    Claim channel;
                    if (defaultChannel.size() > 0) {
                        channel = defaultChannel.get(0);
                    } else {
                        channel = Lbry.ownChannels.get(0);
                    }

                    // if the tip UI is open, then require a valid tip amount to be entered
                    final boolean shouldTip = layoutLivestreamChatTip.getVisibility() == View.VISIBLE;
                    BigDecimal tipAmount = new BigDecimal(0);
                    if (shouldTip) {
                        String amountString = Helper.getValue(inputTipAmount.getText());
                        if (Helper.isNullOrEmpty(amountString)) {
                            showError(getString(R.string.invalid_amount));
                            return;
                        }

                        try {
                            tipAmount = new BigDecimal(amountString);
                        } catch (NumberFormatException nfex) {
                            showError(getString(R.string.invalid_amount));
                            return;
                        }

                        if (tipAmount.doubleValue() > Lbry.getAvailableBalance()) {
                            showError(getString(R.string.insufficient_balance));
                            return;
                        }

                        double minSpend = Helper.MIN_SPEND; // absolute minimum to use, if no creator setting was loaded
                        // check creatorSetting for minimum
                        if (currentCreatorSetting != null &&
                                (currentCreatorSetting.getMinTipAmountCommentValue() > 0 ||
                                        currentCreatorSetting.getMinTipAmountSuperChatValue() > 0)) {
                            // always use min comment value first if set, otherwise, use min superchat value
                            minSpend = currentCreatorSetting.getMinTipAmountCommentValue() > 0 ?
                                    currentCreatorSetting.getMinTipAmountCommentValue() :
                                    currentCreatorSetting.getMinTipAmountSuperChatValue();
                        }

                        if (tipAmount.doubleValue() < minSpend) {
                            showError(getString(R.string.creator_min_spend_required, minSpend));
                            return;
                        }
                    }

                    if (shouldTip) {
                        Helper.setViewEnabled(inputTipAmount, false);
                        Helper.setViewEnabled(buttonCloseTipArea, false);
                    }
                    Helper.setViewEnabled(inputChatMessage, false);
                    Helper.setViewEnabled(buttonSendChatMessage, false);
                    Helper.setViewEnabled(buttonToggleTipArea, false);

                    double dblTipAmount = shouldTip ? tipAmount.doubleValue() : 0;
                    Activity a = getActivity();
                    if (a != null) {
                        ((OdyseeApp) a.getApplication()).getExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Start with the tip request
                                    String supportTxId = null;
                                    if (shouldTip) {
                                        Map<String, Object> tipOptions = new HashMap<>();
                                        tipOptions.put("blocking", true);
                                        tipOptions.put("claim_id", actualClaim.getClaimId());
                                        tipOptions.put("amount", new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).format(dblTipAmount));
                                        tipOptions.put("tip", true);
                                        tipOptions.put("channel_id", channel.getClaimId());

                                        JSONObject response = (JSONObject) Lbry.authenticatedGenericApiCall(Lbry.METHOD_SUPPORT_CREATE, tipOptions, Lbryio.AUTH_TOKEN);
                                        supportTxId = Helper.getJSONString("txid", null, response);
                                        // TODO: show a successful tip message or no?
                                    }

                                    // Channel Sign chat message
                                    JSONObject channelSignParams = new JSONObject();
                                    channelSignParams.put("auth_token", Lbryio.AUTH_TOKEN);
                                    JSONObject jsonChannelSign = Comments.channelSignPrivate(
                                            channelSignParams, channel.getClaimId(),
                                            channel.getName(), chatMessage
                                    );

                                    // Then on to comment creation
                                    JSONObject params = new JSONObject();
                                    params.put("claim_id", actualClaim.getClaimId());
                                    params.put("channel_id", channel.getClaimId());
                                    params.put("comment", chatMessage);
                                    if (!Helper.isNullOrEmpty(supportTxId)) {
                                        params.put("support_tx_id", supportTxId);
                                    }

                                    if (jsonChannelSign.has("signature") && jsonChannelSign.has("signing_ts")) {
                                        params.put("signature", jsonChannelSign.getString("signature"));
                                        params.put("signing_ts", jsonChannelSign.getString("signing_ts"));
                                    }

                                    JSONObject response = (JSONObject) Lbry.parseResponse(Comments.performRequest(params, "comment.Create"));
                                    finishChatSend(true);
                                } catch (ApiCallException | LbryResponseException | JSONException | IOException ex) {
                                    showError(getString(R.string.could_not_send_chat_message));
                                    finishChatSend(false);
                                }
                            }
                        });
                    }
                }
            }
        });

        inputChatMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Helper.setViewEnabled(buttonSendChatMessage, charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
        RecyclerView commentsList = root.findViewById(R.id.file_view_comments_list);
        relatedContentList.setNestedScrollingEnabled(false);
        commentsList.setNestedScrollingEnabled(false);
        LinearLayoutManager relatedContentListLLM = new LinearLayoutManager(getContext());
        LinearLayoutManager commentsListLLM = new LinearLayoutManager(getContext());
        relatedContentList.setLayoutManager(relatedContentListLLM);
        commentsList.setLayoutManager(commentsListLLM);

        GestureDetector.SimpleOnGestureListener playerGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                ImageView seekOverlay = root.findViewById(R.id.seek_overlay);
                Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();

                int width = playerView.getWidth();
                float eventX = e.getX();
                if (eventX < width / 3.0) {
                    if (currentPlayer != null) {
                        currentPlayer.seekTo(Math.max(0, currentPlayer.getCurrentPosition() - 10000));

                        seekOverlay.setVisibility(View.VISIBLE);
                        seekOverlay.setImageResource(R.drawable.ic_rewind);
                    }
                } else if (eventX > width * 2.0 / 3.0) {
                    if (currentPlayer != null) {
                        currentPlayer.seekTo(currentPlayer.getCurrentPosition() + 10000);

                        seekOverlay.setVisibility(View.VISIBLE);
                        seekOverlay.setImageResource(R.drawable.ic_forward);
                    }
                } else {
                    return true;
                }

                if (seekOverlayHandler == null) {
                    seekOverlayHandler = new Handler(Looper.getMainLooper());
                } else {
                    seekOverlayHandler.removeCallbacksAndMessages(null); // Clear pending messages
                }
                seekOverlayHandler.postDelayed(() -> {
                    seekOverlay.setVisibility(View.GONE);
                }, 500);

                return true;
            }
        };
        GestureDetector playerGestureDetector = new GestureDetector(getContext(), playerGestureListener);
        playerView.setOnTouchListener((view, motionEvent) -> playerGestureDetector.onTouchEvent(motionEvent));

        GestureDetector.SimpleOnGestureListener mediaContainerGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (velocityY > 0) {
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        MainActivity activity = (MainActivity) context;
                        activity.onBackPressed();
                    }
                }
                return true;
            }
        };
        mediaContainer.gestureDetector = new GestureDetector(getContext(), mediaContainerGestureListener);
    }

    private void initLiveChatTippingArea() {
        buttonToggleTipArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutLivestreamChatTip.setVisibility(View.VISIBLE);
                inputTipAmount.requestFocus();
            }
        });

        buttonCloseTipArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutLivestreamChatTip.setVisibility(View.GONE);
                inputTipAmount.setText(null);
            }
        });

        inputTipAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    Double value = Double.parseDouble(charSequence.toString());
                    labelTipCredits.setText(value == 1 ? R.string.lbc_singular : R.string.lbc);
                } catch (NumberFormatException ex) {
                    labelTipCredits.setText(R.string.lbc);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void finishChatSend(boolean clearInput) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (clearInput) {
                    inputChatMessage.setText(null);
                    inputTipAmount.setText(null);
                    Helper.setViewVisibility(layoutLivestreamChatTip, View.GONE);
                }
                Helper.setViewEnabled(buttonSendChatMessage, inputChatMessage.getText().length() > 0);
                Helper.setViewEnabled(inputChatMessage, true);
                Helper.setViewEnabled(inputTipAmount, true);
                Helper.setViewEnabled(buttonCloseTipArea, true);
                Helper.setViewEnabled(buttonToggleTipArea, true);
                labelTipCredits.setText(R.string.lbc);
            }
        });
    }

    private void removeNotificationAsSource() {
        // If we arrived here from a notification, navigating to a channel
        // will show the notifications panel and a very weird layout after
        // it.
        // So let's avoid it by removing that flag. User will need to re-open
        // the panel again to keep visiting content from notifications
        Map<String, Object> params = getParams();
        if (params != null && params.containsKey("source")) {
            String notificationSource = (String) params.get("source");

            if ("notification".equalsIgnoreCase(notificationSource)) {
                params.remove("source");
                setParams(params);
            }
        }
    }

    private void updateQualityView(View root) {
        if (root != null) {
            TextView textQuality = root.findViewById(R.id.player_quality);
            if (MainActivity.videoQuality == MainActivity.AUTO_QUALITY_ID) {
                textQuality.setText(AUTO_QUALITY_STRING);
            } else {
                textQuality.setText(String.format("%sp", MainActivity.videoQuality));
            }
        }
    }

    private void deleteCurrentClaim() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null) {
            Helper.setViewVisibility(layoutDisplayArea, View.INVISIBLE);
            Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
            Helper.setViewVisibility(layoutNothingAtLocation, View.GONE);

            AbandonStreamTask task = new AbandonStreamTask(Collections.singletonList(actualClaim.getClaimId()),
                    layoutResolving, Lbryio.AUTH_TOKEN, new AbandonHandler() {
                @Override
                public void onComplete(List<String> successfulClaimIds, List<String> failedClaimIds, List<Exception> errors) {
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        if (failedClaimIds.size() == 0) {
                            showMessage(R.string.content_deleted);
                            MainActivity activity = (MainActivity) context;
                            activity.onBackPressed();
                        } else {
                            showError(getString(R.string.content_failed_delete));
                        }
                    }
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void downloadCurrentClaim() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
            String mediaType = actualClaim.getMediaType();
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    // Get the streaming URL
                    Map<String, Object> params = new HashMap<>();
                    params.put("uri", actualClaim.getPermanentUrl());
                    JSONObject result = (JSONObject) Lbry.parseResponse(Lbry.apiCall(Lbry.METHOD_GET, params));
                    currentMediaSourceOriginalUrl = (String) result.get("streaming_url");

                    // Show file picker
                    MainActivity.startingFilePickerActivity = true;
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(mediaType);
                    intent.putExtra(Intent.EXTRA_TITLE, actualClaim.getTitleOrName());
                    ((MainActivity) context).startActivityForResult(intent, MainActivity.REQUEST_FILE_PICKER);
                } catch (LbryRequestException | LbryResponseException | JSONException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    private void checkStoragePermissionAndStartDownload() {
        Context context = getContext();
        // TODO: Use StorageAccessFramework on Android 13+?
        if (MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
            startDownload();
        } else {
            if (storagePermissionRefusedOnce) {
                showStoragePermissionRefusedError();
                restoreMainActionButton();
                return;
            }

            MainActivity.requestPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    MainActivity.REQUEST_STORAGE_PERMISSION,
                    getString(R.string.storage_permission_rationale_download),
                    context,
                    true);
        }
    }

    public void onStoragePermissionGranted() {
        startDownload();
    }

    public void onStoragePermissionRefused() {
        storagePermissionRefusedOnce = true;
        onDownloadAborted();

        showStoragePermissionRefusedError();
    }

    public void onManageExternalStoragePermissionGranted() {
        // pass
    }

    public void onManageExternalStoragePermissionRefused() {
        // pass
    }

    public void startDownload() {
        downloadInProgress = true;

        View root = getView();
        if (root != null) {
            Helper.setViewVisibility(root.findViewById(R.id.file_view_download_progress), View.VISIBLE);
            ((ImageView) root.findViewById(R.id.file_view_action_download_icon)).setImageResource(R.drawable.ic_stop);
        }

        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (!actualClaim.isFree()) {
            downloadRequested = true;
            onMainActionButtonClicked();
        } else {
            // download the file
            downloadCurrentClaim();
        }
    }

    private void abortDownload() {
        Context context = getContext();
        if (context != null) {
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.remove(currentDownloadId);
        }

        onDownloadAborted();
        downloadProgressScheduler.shutdown();
    }

    private void setLivestreamChatEnabled(boolean enabled) {
        displayChatMessages(enabled, enabled);
    }

    /**
     *
     * @param display false to hide messages list
     * @param chatEnabled
     */
    private void displayChatMessages(boolean display, boolean chatEnabled) {
        Context ctx = getContext();

        boolean forceDisplay = false;
        if (ctx != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            forceDisplay = sp.getBoolean("com.odysee.app.force_display_chat_messages", false);
        }

        boolean shouldDisplay = display && forceDisplay;
        chatHeaderSection.setVisibility(chatEnabled ? View.VISIBLE : View.GONE);
        buttonChatSettings.setVisibility(!shouldDisplay ? View.VISIBLE : View.GONE);
        buttonChatHeaderClose.setVisibility(shouldDisplay ? View.VISIBLE : View.GONE);

        Helper.setViewVisibility(layoutLivestreamChat, shouldDisplay && chatEnabled ? View.VISIBLE : View.GONE);
        Helper.setViewVisibility(layoutActionsArea, !shouldDisplay ? View.VISIBLE : View.GONE);
        Helper.setViewVisibility(layoutCommentsArea, !shouldDisplay && !chatEnabled ? View.VISIBLE : View.GONE);
        Helper.setViewVisibility(layoutRelatedContentArea, !shouldDisplay ? View.VISIBLE : View.GONE);
        Helper.setViewVisibility(dividerDescriptionArea, !shouldDisplay ? View.VISIBLE : View.GONE);
        Helper.setViewVisibility(dividerRelatedContentArea, !shouldDisplay ? View.VISIBLE : View.GONE);
    }

    private void renderClaim() {
        Claim claimToRender = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (claimToRender == null) {
            return;
        }
        if (!claimToRender.hasSource()) {
            Context context = getContext();
            View root = getView();
            String urlResult = getLivestreamUrl();
            if (claimToRender.getThumbnailUrl() != null && context != null && root != null && !claimToRender.isLive()) {
                if (urlResult != null && urlResult.equalsIgnoreCase("notlive")) {
                    renderPublisherNotBroadcasting(claimToRender);
                } else {
                    fileClaim.setLive(true);
                    fileClaim.setLivestreamUrl(urlResult);
                }
            }

            if (jsonData != null) {
                JSONObject j = jsonData.get(fileClaim.getSigningChannel().getClaimId());

                if (j != null && j.has("Start")) {
                    try {
                        String livestreamStart = j.getString("Start");
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
                        ZonedDateTime zonedStart = ZonedDateTime.parse(livestreamStart, dtf);

                        livestreamStartingMillis = zonedStart.toInstant().toEpochMilli();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            // livestream, so we load up the messages and initialise the websocket
            initLivestreamChat();
            isLivestream = true;
        }

        Helper.setViewVisibility(layoutLoadingState, View.GONE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.GONE);

/*
        if (claim.getTags().contains("disable-support") || claim.getSigningChannel().getTags().contains("disable-support"))
            Helper.setViewVisibility(tipButton, View.GONE);
        else
            Helper.setViewVisibility(tipButton, View.VISIBLE);
*/

        if (fileClaim != null && !fileClaim.isLive()) {
            loadViewCount();
        }
        loadReactions(claimToRender);
        checkIsFollowing();

        View root = getView();
        if (root != null) {
            Context context = getContext();

            root.findViewById(R.id.file_view_scroll_view).scrollTo(0, 0);
            Helper.setViewVisibility(layoutDisplayArea, View.VISIBLE);

            ImageView descIndicator = root.findViewById(R.id.file_view_desc_toggle_arrow);
            descIndicator.setImageResource(R.drawable.ic_arrow_dropdown);

            boolean hasDescription = !Helper.isNullOrEmpty(claimToRender.getDescription());
            boolean hasTags = claimToRender.getTags() != null && claimToRender.getTags().size() > 0;

            root.findViewById(R.id.file_view_description).setVisibility(hasDescription ? View.VISIBLE : View.GONE);
            root.findViewById(R.id.file_view_tag_area).setVisibility(hasTags ? View.VISIBLE : View.GONE);
            if (hasTags && !hasDescription) {
                root.findViewById(R.id.file_view_tag_area).setPadding(0, 0, 0, 0);
            }

            root.findViewById(R.id.file_view_description_area).setVisibility(View.GONE);
            ((TextView) root.findViewById(R.id.file_view_title)).setText(claimToRender.getTitle());
            ((TextView) root.findViewById(R.id.file_view_description)).setText(claimToRender.getDescription());
            ((TextView) root.findViewById(R.id.file_view_publisher_name)).setText(
                    Helper.isNullOrEmpty(claimToRender.getPublisherName()) ? getString(R.string.anonymous) : claimToRender.getPublisherName());

            Claim signingChannel = claimToRender.getSigningChannel();
            boolean hasPublisher = signingChannel != null;
            boolean hasPublisherThumbnail = hasPublisher && !Helper.isNullOrEmpty(signingChannel.getThumbnailUrl());
            root.findViewById(R.id.file_view_publisher_avatar).setVisibility(hasPublisher ? View.VISIBLE : View.GONE);
            root.findViewById(R.id.file_view_publisher_thumbnail).setVisibility(hasPublisherThumbnail ? View.VISIBLE : View.INVISIBLE);
            root.findViewById(R.id.file_view_publisher_no_thumbnail).setVisibility(!hasPublisherThumbnail ? View.VISIBLE : View.INVISIBLE);
            if (hasPublisher) {
                int bgColor = Helper.generateRandomColorForValue(signingChannel.getClaimId());
                Helper.setIconViewBackgroundColor(root.findViewById(R.id.file_view_publisher_no_thumbnail), bgColor, false, context);
                if (hasPublisherThumbnail && context != null) {
                    ViewGroup.LayoutParams lp = root.findViewById(R.id.file_view_publisher_thumbnail).getLayoutParams();
                    Glide.with(context.getApplicationContext()).load(signingChannel.getThumbnailUrl(Utils.CHANNEL_THUMBNAIL_WIDTH, Utils.CHANNEL_THUMBNAIL_HEIGHT, Utils.CHANNEL_THUMBNAIL_Q)).
                            apply(RequestOptions.circleCropTransform()).into((ImageView) root.findViewById(R.id.file_view_publisher_thumbnail));
                }
                ((TextView) root.findViewById(R.id.file_view_publisher_thumbnail_alpha)).
                        setText(signingChannel.getName() != null ? signingChannel.getName().substring(1, 2).toUpperCase() : null);

            }

            String publisherTitle = signingChannel != null ? signingChannel.getTitle() : null;
            TextView textPublisherTitle = root.findViewById(R.id.file_view_publisher_title);
            textPublisherTitle.setVisibility(Helper.isNullOrEmpty(publisherTitle) ? View.GONE : View.VISIBLE);
            textPublisherTitle.setText(publisherTitle);

            RecyclerView descTagsList = root.findViewById(R.id.file_view_tag_list);
            FlexboxLayoutManager flm = new FlexboxLayoutManager(context);
            descTagsList.setLayoutManager(flm);

            List<Tag> tags = claimToRender.getTagObjects();
            TagListAdapter tagListAdapter = new TagListAdapter(tags, context);
            tagListAdapter.setClickListener(new TagListAdapter.TagClickListener() {
                @Override
                public void onTagClicked(Tag tag, int customizeMode) {
                    if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_NONE) {
                        Context ctx = getContext();
                        if (ctx instanceof MainActivity) {
                            ((MainActivity) ctx).openAllContentFragmentWithTag(tag.getName());
                        }
                    }
                }
            });
            descTagsList.setAdapter(tagListAdapter);
            root.findViewById(R.id.file_view_tag_area).setVisibility(tags.size() > 0 ? View.VISIBLE : View.GONE);

            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_media_meta_container).setVisibility(View.VISIBLE);

            root.findViewById(R.id.file_view_main_action_loading).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_play).setVisibility(claimToRender.isPlayable() ? View.VISIBLE : View.INVISIBLE);

            Claim.GenericMetadata metadata = claimToRender.getValue();
            if (!Helper.isNullOrEmpty(claimToRender.getThumbnailUrl()) && context != null) {
                ImageView thumbnailView = root.findViewById(R.id.file_view_thumbnail);
                Glide.with(context.getApplicationContext()).asBitmap().load(
                        claimToRender.getThumbnailUrl(Utils.STREAM_THUMBNAIL_WIDTH, Utils.STREAM_THUMBNAIL_HEIGHT, Utils.STREAM_THUMBNAIL_Q)).centerCrop().into(thumbnailView);
            } else {
                // display first x letters of claim name, with random background
            }

            root.findViewById(R.id.file_view_main_action_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onMainActionButtonClicked();
                }
            });
            root.findViewById(R.id.file_view_media_meta_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onMainActionButtonClicked();
                }
            });
            root.findViewById(R.id.file_view_download_unsuported_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!downloadInProgress) {
                        checkStoragePermissionAndStartDownload();
                    }
                }
            });

            if (metadata instanceof Claim.StreamMetadata) {
                Claim.StreamMetadata streamMetadata = (Claim.StreamMetadata) metadata;
                updatePublishTime(streamMetadata, claimToRender);

                Fee fee = streamMetadata.getFee();
                if (fee != null && Helper.parseDouble(fee.getAmount(), 0) > 0) {
                    root.findViewById(R.id.file_view_fee_container).setVisibility(View.VISIBLE);
                    ((TextView) root.findViewById(R.id.file_view_fee)).setText(
                            Helper.shortCurrencyFormat(claimToRender.getActualCost(Lbryio.LBCUSDRate).doubleValue()));
                }
            }

            boolean isAnonymous = claimToRender.getSigningChannel() == null;
            View iconFollow = root.findViewById(R.id.file_view_icon_follow);
            View iconUnfollow = root.findViewById(R.id.file_view_icon_unfollow);
            if (isAnonymous) {
                if (iconFollow.getVisibility() == View.VISIBLE) {
                    iconFollow.setVisibility(View.INVISIBLE);
                }
                if (iconUnfollow.getVisibility() == View.VISIBLE) {
                    iconUnfollow.setVisibility(View.INVISIBLE);
                }
            }

            MaterialButton mainActionButton = root.findViewById(R.id.file_view_main_action_button);
            if (claimToRender.isPlayable()) {
                mainActionButton.setText(R.string.play);
            } else if (claimToRender.isViewable()) {
                mainActionButton.setText(R.string.view);
            } else {
                mainActionButton.setText(R.string.download);
            }
        }

        setLivestreamChatEnabled(!claimToRender.hasSource());

        if (claimToRender.isFree() && Helper.isNullOrEmpty(commentHash)) {
            if (claimToRender.isPlayable() || (!Lbry.SDK_READY && Lbryio.isSignedIn())) {
                if (MainActivity.nowPlayingClaim != null && MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(claimToRender.getClaimId())) {
                    // claim already playing
                    showExoplayerView();
                    playMedia();

                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).hideGlobalNowPlaying();
                    }
                } else {
                    if (alwaysAutoplay) {
                        onMainActionButtonClicked(true);
                        alwaysAutoplay = false;
                    } else {
                        String mediaAutoplay = Objects.requireNonNull((MainActivity) (getActivity())).mediaAutoplayEnabled();
                        if (MainActivity.nowPlayingClaim == null) {
                            if (!mediaAutoplay.equals(MainActivity.APP_SETTING_AUTOPLAY_NEVER) || claimToRender.isViewable()) {
                                onMainActionButtonClicked();
                            } else if (claimToRender.isPlayable()) {
                                if (root != null) {
                                    root.findViewById(R.id.file_view_main_action_button).setVisibility(View.INVISIBLE);
                                }
                            }
                        } else {
                            if (mediaAutoplay.equals(MainActivity.APP_SETTING_AUTOPLAY_ALWAYS)) {
                                onMainActionButtonClicked();
                            } else if (claimToRender.isViewable()) {
                                restoreMainActionButton();
                            }
                        }
                    }
                }
            } else if (claimToRender.isViewable() && Lbry.SDK_READY) {
                onMainActionButtonClicked();
            } else if (!Lbry.SDK_READY) {
                restoreMainActionButton();
            }
        } else {
            restoreMainActionButton();
        }

        checkRewardsDriver();
        checkOwnClaim();
    }

    private void checkAndLoadRelatedContent() {
        View root = getView();
        if (root != null) {
            RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
            if (relatedContentList == null || relatedContentList.getAdapter() == null || relatedContentList.getAdapter().getItemCount() == 0) {
                loadRelatedContent();
            }
        }
    }

    private void checkAndLoadComments() {
        checkAndLoadComments(false);
    }

    private void checkAndLoadComments(boolean forceReload) {
        View root = getView();
        if (root != null) {
            Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;

            View expandCommentArea = root.findViewById(R.id.expand_commentarea_button);
            View commentsDisabledText = root.findViewById(R.id.file_view_disabled_comments);
            RecyclerView commentsList = root.findViewById(R.id.file_view_comments_list);

            commentLoadingArea.setVisibility(View.VISIBLE);
            commentEnabledCheck.checkCommentStatus(
                    actualClaim.getSigningChannel().getClaimId(), actualClaim.getSigningChannel().getName(), isEnabled -> {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        if (isEnabled) {
                            showComments(expandCommentArea, commentsDisabledText, commentsList);
                            if ((commentsList != null && forceReload) || (commentsList == null || commentsList.getAdapter() == null || commentsList.getAdapter().getItemCount() == 0)) {
                                loadComments();
                            }
                        } else {
                            hideComments(expandCommentArea, commentsDisabledText, commentsList);
                        }
                        commentLoadingArea.setVisibility(View.GONE);
                    });
                }
            });
        }
    }

    private void showComments(View root, View commentsDisabledText, View commentsList) {
        root.findViewById(R.id.expand_commentarea_button).setVisibility(View.VISIBLE);
        Helper.setViewVisibility(commentsDisabledText, View.GONE);
        Helper.setViewVisibility(commentsList, View.VISIBLE);
    }

    private void hideComments(View root, View commentsDisabledText, View commentsList) {
        root.findViewById(R.id.expand_commentarea_button).setVisibility(View.GONE);
        Helper.setViewVisibility(commentsDisabledText, View.VISIBLE);
        Helper.setViewVisibility(commentsList, View.GONE);
    }

    private void showUnsupportedView() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_unsupported_container).setVisibility(View.VISIBLE);
        }
    }

    private void showExoplayerView() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.VISIBLE);
        }
    }

    private void playMedia() {
        boolean newPlayerCreated = false;

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;

            if (MainActivity.playerManager == null) {
                MainActivity.playerManager = new PlayerManager(activity);
                activity.setNetworkCallback();

                MainActivity.playerCache =
                        new SimpleCache(context.getCacheDir(),
                                new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 256), new ExoDatabaseProvider(context));

                activity.initMediaSession();
                activity.initPlaybackNotification();

                MainActivity.playerManager.setListener(new PlayerManager.Listener() {
                    @Override
                    public void onPlayerChanged(long previousPlaybackMs) {
                        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                        if (!Helper.isNullOrEmpty(currentMediaSourceUrl)) {
                            MainActivity.playerManager.initializeCurrentPlayer(
                                    currentMediaSourceUrl, previousPlaybackMs, actualClaim, context);
                        }
                        setPlayerForPlayerView();
                        activity.initMediaSession();
                        activity.initPlaybackNotification();
                    }
                });

                newPlayerCreated = true;
            }
        }

        Claim claimToPlay = collectionClaimItem != null ? collectionClaimItem : fileClaim;

        View root = getView();
        if (root != null) {
            Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();

            StyledPlayerView view = root.findViewById(R.id.file_view_exoplayer_view);
            view.setShutterBackgroundColor(Color.TRANSPARENT);
            view.setPlayer(currentPlayer);
            currentPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                    isPlaying = playWhenReady;
                    Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
                }

                @Override
                public void onTrackSelectionParametersChanged(@NotNull TrackSelectionParameters parameters) {
                    updateQualityView(getView());
                }
            });
            view.setUseController(true);
            if (context instanceof MainActivity) {
                ((MainActivity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            if (MainActivity.nowPlayingClaim != null &&
                    MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(claimToPlay.getClaimId()) &&
                    !newPlayerCreated) {
                // if the claim is already playing, we don't need to reload the media source
                if (currentPlayer != null) {
                    currentPlayer.setPlayWhenReady(true);
                    playbackStarted = true;

                    if (context instanceof MainActivity) {
                        MainActivity activity = (MainActivity) context;
                        activity.displayCurrentlyPlayingVideo();
                    }

                    // reconnect the app player
                    if (fileViewPlayerListener != null) {
                        currentPlayer.addListener(fileViewPlayerListener);
                    }
                    setPlayerForPlayerView();
                    loadAndScheduleDurations();
                }
                return;
            }

            if (currentPlayer != null) {
                if (fileViewPlayerListener != null) {
                    currentPlayer.addListener(fileViewPlayerListener);
                }
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setNowPlayingClaim(claimToPlay, currentUrl);
                }

                currentPlayer.setPlayWhenReady(true);

                if (claimToPlay.hasSource()) {
                    getStreamingUrlAndInitializePlayer(claimToPlay);
                } else {
                    if (!Helper.isNullOrEmpty(claimLivestreamUrl)) {
                        currentMediaSourceUrl = claimLivestreamUrl;
                    } else {
                        currentMediaSourceUrl = getLivestreamUrl();
                    }

                    if (currentMediaSourceUrl != null && currentMediaSourceUrl.equals("notlive")) {
                        if (claimToPlay.getThumbnailUrl() != null && context != null) {
                            ImageView thumbnailView = root.findViewById(R.id.file_view_livestream_thumbnail);
                            Glide.with(context.getApplicationContext()).
                                    asBitmap().
                                    load(claimToPlay.getThumbnailUrl()).
                                    apply(RequestOptions.circleCropTransform()).
                                    into(thumbnailView);
                        }

                        root.findViewById(R.id.file_view_livestream_not_live).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
                        TextView userNotStreaming = root.findViewById(R.id.user_not_streaming);
                        userNotStreaming.setText(getString(R.string.user_not_live_yet, claimToPlay.getPublisherName()));
                        userNotStreaming.setVisibility(View.VISIBLE);
                        return;
                    }

                    MainActivity.videoIsTranscoded = true;
                    MainActivity.playerManager.initializeCurrentPlayer(
                            currentMediaSourceUrl, C.TIME_UNSET, fileClaim, getContext());
                }
            }
        }
    }

    private void getStreamingUrlAndInitializePlayer(Claim theClaim) {
        Activity a = getActivity();
        if (a != null) {
            ((OdyseeApp) a.getApplication()).getExecutor().execute(() -> {
                try {
                    // Get the streaming URL
                    Map<String, Object> params = new HashMap<>();
                    params.put("uri", theClaim.getPermanentUrl());
                    JSONObject result = (JSONObject) Lbry.parseResponse(Lbry.apiCall(Lbry.METHOD_GET, params));
                    if (result != null) {
                        String sourceUrl = (String) result.get("streaming_url");
                        currentMediaSourceUrl = sourceUrl;

                        // Get the stream type
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(sourceUrl)
                                .head()
                                .build();
                        try (Response response = client.newCall(request).execute()) {
                            String requestUrl = response.request().url().toString();
                            boolean requestRedirected = response.priorResponse() != null && response.priorResponse().isRedirect();
                            MainActivity.videoIsTranscoded = requestRedirected && response.isSuccessful() && requestUrl.endsWith("m3u8");
                            currentMediaSourceUrl = MainActivity.videoIsTranscoded ? requestUrl : sourceUrl;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (MainActivity.playerManager != null) { // null pointer check
                                Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
                                MainActivity.playerManager.initializeCurrentPlayer(
                                        currentMediaSourceUrl, C.TIME_UNSET, actualClaim, getContext());
                            }
                        });
                    }
                } catch (LbryRequestException | LbryResponseException | JSONException ex) {
                    // TODO: How does error handling work here
                    ex.printStackTrace();
                }
            });
        }
    }

    /**
     * Call this to get the livestreaming URL for the media. This is to be used when we directly arrive here
     * from outside the app, like through a deep link
     * @return The URL to connect to get the video stream, usually a link to an .M3U8 file
     */
    @WorkerThread
    private String getLivestreamUrl() {
        // User could land on this fragment from a deep-link. That means the livestream would have not been resolved,
        // so it needs to be done
        String jsonDataUrl = null;
        Activity a = getActivity();

        if (a != null) {
            try {
                ChannelLiveStatus callable = new ChannelLiveStatus(Collections.singletonList(fileClaim.getSigningChannel().getClaimId()));
                Future<Map<String, JSONObject>> future = ((OdyseeApp) a.getApplication()).getExecutor().submit(callable);

                jsonData = future.get();
                jsonDataUrl = getLivestreamUrl(jsonData.get(fileClaim.getSigningChannel().getClaimId()));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return jsonDataUrl;
    }

    @AnyThread
    @NonNull
    private String getLivestreamUrl(JSONObject jsonData) {
        try {
            if (jsonData != null && jsonData.has("Live") && jsonData.getBoolean("Live") && jsonData.has("VideoURL")) {
                return jsonData.getString("VideoURL");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "notlive";
    }

    private void setPlaybackSpeedToDefault() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            int speed = ((MainActivity) context).playbackDefaultSpeed();
            setPlaybackSpeed(MainActivity.playerManager.getCurrentPlayer(), speed);
        }
    }

    private void setPlaybackSpeed(Player player, int speedId) {
        float speed = speedId / 100.0f;
        player.setPlaybackSpeed(speed);
    }

    private void setPlayerQualityToDefault() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            boolean isOnMobileNetwork = isMeteredNetwork(context);
            int quality = isOnMobileNetwork ?
                    ((MainActivity) context).mobileDefaultQuality() :
                    ((MainActivity) context).wifiDefaultQuality();
            ((MainActivity) context).setPlayerQuality(MainActivity.playerManager.getCurrentPlayer(), quality);

            updateQualityView(getView());
        }
    }

    private boolean isMeteredNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager != null && connectivityManager.getActiveNetwork() != null && connectivityManager.isActiveNetworkMetered();
    }

    private void resetViewCount() {
        View root = getView();
        if (root != null) {
            TextView textViewCount = root.findViewById(R.id.file_view_view_count);
            Helper.setViewText(textViewCount, null);
            Helper.setViewVisibility(textViewCount, View.GONE);
        }
    }
    private void resetFee() {
        View root = getView();
        if (root != null) {
            TextView feeView = root.findViewById(R.id.file_view_fee);
            feeView.setText(null);
            Helper.setViewVisibility(root.findViewById(R.id.file_view_fee_container), View.GONE);
        }
    }

    private void loadViewCount() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null) {
            FetchStatCountTask task = new FetchStatCountTask(
                    FetchStatCountTask.STAT_VIEW_COUNT, actualClaim.getClaimId(), null, new FetchStatCountTask.FetchStatCountHandler() {
                @Override
                public void onSuccess(List<Integer> counts) {
                    try {
                        int count = counts.get(0);
                        String displayText = count != 0
                                ? getResources().getQuantityString(R.plurals.view_count, count, NumberFormat.getInstance().format(count))
                                : getResources().getString(R.string.no_views_count);
                        View root = getView();
                        if (root != null) {
                            TextView textViewCount = root.findViewById(R.id.file_view_view_count);
                            Helper.setViewText(textViewCount, displayText);
                            Helper.setViewVisibility(textViewCount, View.VISIBLE);
                        }
                    } catch (IllegalStateException ex) {
                        // pass
                    }
                }

                @Override
                public void onError(Exception error) {
                    // pass
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void viewMedia() {
        Claim claimToView = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        String mediaType = claimToView.getMediaType();
        View root = getView();
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Get the streaming URL
                Map<String, Object> params = new HashMap<>();
                params.put("uri", claimToView.getPermanentUrl());
                JSONObject result = (JSONObject) Lbry.parseResponse(Lbry.apiCall(Lbry.METHOD_GET, params));
                String sourceUrl = (String) result.get("streaming_url");

                if (mediaType.startsWith("image")) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        View container = root.findViewById(R.id.file_view_imageviewer_container);
                        PhotoView photoView = root.findViewById(R.id.file_view_imageviewer);

                        Context context = getContext();
                        if (context != null) {
                            Glide.with(context.getApplicationContext()).load(sourceUrl).centerInside().into(photoView);
                        }
                        container.setVisibility(View.VISIBLE);
                    });
                } else if (Arrays.asList("text/markdown", "text/md").contains(mediaType.toLowerCase())) {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(sourceUrl)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        ResponseBody body = response.body();
                        if (body != null) {
                            String html = buildMarkdownHtml(body.string());
                            new Handler(Looper.getMainLooper()).post(() -> {
                                View container = root.findViewById(R.id.file_view_webview_container);
                                initWebView(root);
                                applyThemeToWebView();

                                if (webView != null) {
                                    webView.loadData(Base64.encodeToString(html.getBytes(), Base64.NO_PADDING), "text/html", "base64");
                                }
                                container.setVisibility(View.VISIBLE);
                            });
                        }
                    }
                }
            } catch (LbryRequestException | LbryResponseException | JSONException | IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void loadReactions(Claim c) {
        if (futureReactions != null) {
            futureReactions.cancel(true);
        }

        Activity a = getActivity();

        if (a != null) {
            if (reactions == null) {
                reactions = new Reactions();
            }

            Runnable runnable = () -> {
                MainActivity activity = (MainActivity) getActivity();
                if ((activity != null && !activity.isBatterySaverMode()) || reactions.getLastUpdateTimestamp() == 0) {
                    Map<String, String> options = new HashMap<>();
                    options.put("claim_ids", c.getClaimId());

                    JSONObject data;
                    try {
                        data = (JSONObject) Lbryio.parseResponse(Lbryio.call("reaction", "list", options, Helper.METHOD_POST, getContext()));

                        if (data != null && data.has("others_reactions")) {
                            JSONObject othersReactions = (JSONObject) data.get("others_reactions");
                            if (othersReactions.has(c.getClaimId())) {
                                int likesFromOthers = ((JSONObject) othersReactions.get(c.getClaimId())).getInt("like");
                                int dislikesFromOthers = ((JSONObject) othersReactions.get(c.getClaimId())).getInt("dislike");
                                reactions.setOthersLikes(likesFromOthers);
                                reactions.setOthersDislikes(dislikesFromOthers);
                            }
                        }
                        if (data != null && data.has("my_reactions")) {
                            JSONObject othersReactions = (JSONObject) data.get("my_reactions");
                            if (othersReactions.has(c.getClaimId())) {
                                int likes = ((JSONObject) othersReactions.get(c.getClaimId())).getInt("like");
                                reactions.setLiked(likes > 0);
                                c.setLiked(likes > 0);
                                int dislikes = ((JSONObject) othersReactions.get(c.getClaimId())).getInt("dislike");
                                reactions.setDisliked(dislikes > 0);
                                c.setDisliked(dislikes > 0);
                            }

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(new Date());
                            reactions.setLastUpdateTimestamp(cal.getTimeInMillis());

                            updateContentReactions();
                        }
                    } catch (LbryioRequestException | LbryioResponseException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            try {
                futureReactions = ((OdyseeApp) a.getApplication()).getScheduledExecutor().scheduleWithFixedDelay(runnable, 0, 5, TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateContentReactions() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                int likes = reactions.isLiked() ? reactions.getOthersLikes() + 1 : reactions.getOthersLikes();
                int dislikes = reactions.isDisliked() ? reactions.getOthersDislikes() + 1 : reactions.getOthersDislikes();
                likeReactionAmount.setText(String.valueOf(likes));
                dislikeReactionAmount.setText(String.valueOf(dislikes));

                int inactiveColor = 0;
                int fireActive = 0;
                int slimeActive = 0;
                Context context = getContext();
                if (context != null) {
                    inactiveColor = ContextCompat.getColor(context, R.color.darkForeground);
                    fireActive = ContextCompat.getColor(context, R.color.fireActive);
                    slimeActive = ContextCompat.getColor(context, R.color.slimeActive);
                }
                if (reactions.isLiked()) {
                    likeReactionIcon.setColorFilter(fireActive, PorterDuff.Mode.SRC_IN);
                    likeReactionAmount.setTextColor(fireActive);
                } else {
                    likeReactionIcon.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_IN);
                    likeReactionAmount.setTextColor(inactiveColor);
                }

                if (reactions.isDisliked()) {
                    dislikeReactionIcon.setColorFilter(slimeActive, PorterDuff.Mode.SRC_IN);
                    dislikeReactionAmount.setTextColor(slimeActive);
                } else {
                    dislikeReactionIcon.setColorFilter(inactiveColor, PorterDuff.Mode.SRC_IN);
                    dislikeReactionAmount.setTextColor(inactiveColor);
                }
            }
        });
    }

    private Map<String, Reactions> loadReactions(List<Comment> comments) {
        List<String> commentIds = new ArrayList<>();

        Activity a = getActivity();

        if (a != null) {
            for (Comment c: comments) {
                commentIds.add(c.getId());
            }

            Future<Map<String, Reactions>> future = ((OdyseeApp) a.getApplication()).getExecutor().submit(() -> {
                Comments.checkCommentsEndpointStatus();
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("comment_ids", TextUtils.join(",", commentIds));

                AccountManager am = AccountManager.get(getContext());
                Account odyseeAccount = Helper.getOdyseeAccount(am.getAccounts());
                if (odyseeAccount != null) {
                    jsonParams.put("auth_token", am.peekAuthToken(odyseeAccount, "auth_token_type"));
                }

                if (!Lbry.ownChannels.isEmpty()) {
                    jsonParams.put("channel_id", Lbry.ownChannels.get(0).getClaimId());
                    jsonParams.put("channel_name", Lbry.ownChannels.get(0).getName());

                    try {
                        JSONObject jsonChannelSign = Comments.channelSignName(jsonParams, jsonParams.getString("channel_id"), jsonParams.getString("channel_name"));

                        if (jsonChannelSign.has("signature") && jsonChannelSign.has("signing_ts")) {
                            jsonParams.put("signature", jsonChannelSign.getString("signature"));
                            jsonParams.put("signing_ts", jsonChannelSign.getString("signing_ts"));
                        }
                    } catch (ApiCallException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                Map<String, Reactions> result = new HashMap<>();
                try {
                    Response response = Comments.performRequest(jsonParams, "reaction.List");
                    String responseString = response.body().string();
                    response.close();

                    JSONObject jsonResponse = new JSONObject(responseString);

                    if (jsonResponse.has("result")) {
                        JSONObject jsonResult = jsonResponse.getJSONObject("result");
                        if (jsonResult.has("others_reactions")) {
                            JSONObject responseOthersReactions = jsonResult.getJSONObject("others_reactions");
                            responseOthersReactions.keys().forEachRemaining(key -> {
                                try {
                                    result.put(key, getMyReactions(jsonResult, responseOthersReactions, key));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            });

            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e ) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Nullable
    private Reactions getMyReactions(JSONObject jsonResult, JSONObject responseOthersReactions, String key) throws JSONException {
        JSONObject value = (JSONObject) responseOthersReactions.get(key);
        Reactions reactions = getReactionsForValue(value);

        if (jsonResult.has("my_reactions")) {
            JSONObject responseMyReactions = jsonResult.getJSONObject("my_reactions");
            if (responseMyReactions.has(key) && reactions != null) {
                JSONObject myReaction = (JSONObject) responseMyReactions.get(key);
                reactions.setLiked(myReaction.getInt("like") > 0);
                reactions.setDisliked(myReaction.getInt("dislike") > 0);
            }
        }
        return reactions;
    }


    private Reactions getReactionsForValue(JSONObject value) {
        try {
            return new Reactions(value.getInt("like"), value.getInt("dislike"));
        } catch (JSONException e) {
            return null;
        }
    }

    private void onMainActionButtonClicked() {
        onMainActionButtonClicked(false);
    }

    private void onMainActionButtonClicked(boolean fromPlaylist) {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        checkAndResetNowPlayingClaim(fromPlaylist);

        // Check if the claim is free
        Claim.GenericMetadata metadata = actualClaim.getValue();
        if (metadata instanceof Claim.StreamMetadata) {
            View root = getView();
            if (root != null) {
                root.findViewById(R.id.file_view_main_action_button).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.file_view_main_action_loading).setVisibility(View.VISIBLE);
            }
            if (actualClaim.getFile() == null && !actualClaim.isFree()) {
                checkAndConfirmPurchaseUrl();
            } else {
                handleMainActionForClaim();
            }
        } else {
            showError(getString(R.string.cannot_view_claim));
        }
    }

    private void checkAndConfirmPurchaseUrl() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null) {
            PurchaseListTask task = new PurchaseListTask(actualClaim.getClaimId(), null, new ClaimSearchResultHandler() {
                @Override
                public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                    boolean purchased = false;
                    if (claims.size() == 1) {
                        Claim purchasedClaim = claims.get(0);
                        if (actualClaim.getClaimId().equalsIgnoreCase(purchasedClaim.getClaimId())) {
                            // already purchased
                            purchased = true;
                        }
                    }

                    if (purchased) {
                        handleMainActionForClaim();
                    } else {
                        restoreMainActionButton();
                        confirmPurchaseUrl();
                    }
                }

                @Override
                public void onError(Exception error) {
                    // pass
                    restoreMainActionButton();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void confirmPurchaseUrl() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null) {
            Fee fee = ((Claim.StreamMetadata) actualClaim.getValue()).getFee();
            double cost = actualClaim.getActualCost(Lbryio.LBCUSDRate).doubleValue();
            String formattedCost = Helper.LBC_CURRENCY_FORMAT.format(cost);
            Context context = getContext();
            if (context != null) {
                try {
                    String message = getResources().getQuantityString(
                            R.plurals.confirm_purchase_message,
                            cost == 1 ? 1 : 2,
                            actualClaim.getTitle(),
                            formattedCost.equals("0") ? Helper.FULL_LBC_CURRENCY_FORMAT.format(cost) : formattedCost);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context).
                            setTitle(R.string.confirm_purchase).
                            setMessage(message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("uri", currentUrl);
                                    bundle.putString("paid", "true");
                                    bundle.putDouble("amount", Helper.parseDouble(fee.getAmount(), 0));
                                    bundle.putDouble("lbc_amount", cost);
                                    bundle.putString("currency", fee.getCurrency());
                                    LbryAnalytics.logEvent(LbryAnalytics.EVENT_PURCHASE_URI, bundle);

                                    View root = getView();
                                    if (root != null) {
                                        root.findViewById(R.id.file_view_main_action_button).setVisibility(View.INVISIBLE);
                                        root.findViewById(R.id.file_view_main_action_loading).setVisibility(View.VISIBLE);
                                    }
                                    handleMainActionForClaim();
                                }
                            }).setNegativeButton(R.string.no, null);
                    builder.show();
                } catch (IllegalStateException ex) {
                    // pass
                }
            }
        }
    }

    private void resolvePlaylistClaimsAndPlayFirst() {
        if (playlistResolved) {
            return;
        }

        Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
        Helper.setViewVisibility(layoutResolving, View.VISIBLE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.GONE);
        Helper.setViewVisibility(layoutDisplayArea, View.GONE);

        Activity a = getActivity();
        OdyseeApp app = (OdyseeApp) a.getApplication();

        Map<String, Object> options = new HashMap<>();
        options.put("claim_type", "stream");
        options.put("page_size", 50);
        options.put("claim_ids", fileClaim.getClaimIds());
        options.put("order_by", "release_time");

        int pages = (int) Math.ceil(fileClaim.getClaimIds().size() / 50.0);
        Collection<Callable<List<Claim>>> callables = new ArrayList<>(pages);
        for (int i = 1; i <= pages; i++) {
            options.put("page", i);
            Map<String, Object> optionsClone = new HashMap<>(options);
            callables.add(() -> Lbry.claimSearch(optionsClone, Lbry.API_CONNECTION_STRING).getClaims());
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Future<List<Claim>>> results = app.getExecutor().invokeAll(callables);
                    List<Claim> claims = new ArrayList<>();
                    for (Future<List<Claim>> f : results) {
                        if (!f.isCancelled()) {
                            List<Claim> items = f.get();
                            if (items != null) {
                                claims.addAll(items);
                            }
                        }
                    }

                    playlistResolved = true;

                    Collections.sort(claims, Ordering.explicit(fileClaim.getClaimIds()).onResultOf(Claim::getClaimId));

                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            playlistClaims = claims;
                            if (playlistClaims.size() > 0) {
                                OdyseeCollection collection = OdyseeCollection.fromClaim(fileClaim, new ArrayList<>());
                                collection.setClaims(playlistClaims);
                                currentCollectionId = collection.getId();

                                Context context = getContext();
                                if (context instanceof MainActivity) {
                                    ((MainActivity) context).setCurrentPlaylist(collection);
                                }

                                playClaimFromCollection(playlistClaims.get(0), 0);
                            }

                            // TODO: This block of code needs to be moved to being handled by the playlist overlay
                            /*
                            relatedContentAdapter = new ClaimListAdapter(playlistClaims.subList(playlistPos, playlistPos + 25), getContext());
                            relatedContentAdapter.setListener(FileViewFragment.this);
                            scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                @Override
                                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                    if (scrollY == 0) {
                                        if (playlistPos == 0) return;
                                        playlistPos -= 25;
                                        oldPlaylistPos = playlistPos;
                                        relatedContentAdapter.setItems(playlistClaims.subList(playlistPos, playlistPos + 25));
                                        scrollView.postDelayed(() -> {
                                            v.scrollTo(0, (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight() - 1));
                                        }, 500);
                                    }
                                    if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                                        if (playlistPos != oldPlaylistPos || playlistPos + 25 >= playlistClaims.size()) return;
                                        playlistPos += 25;
                                        if (playlistClaims.size() >= playlistPos + 25) {
                                            relatedContentAdapter.setItems(playlistClaims.subList(playlistPos, playlistPos + 25));
                                        } else {
                                            relatedContentAdapter.setItems(playlistClaims.subList(playlistPos, playlistClaims.size() - 1));
                                        }
                                        scrollView.postDelayed(() -> {
                                            v.scrollTo(0, 1);
                                            oldPlaylistPos = playlistPos;
                                        }, 500);
                                    }
                                }
                            });

                            View root = getView();
                            if (root != null) {
                                RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
                                relatedContentList.setAdapter(relatedContentAdapter);
                            }*/
                        }
                    });
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void handleMainActionForClaim() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim.isPlayable()) {
            startTimeMillis = System.currentTimeMillis();
            showExoplayerView();
            playMedia();
        } else if (actualClaim.isViewable()) {
            viewMedia();
        } else {
            showUnsupportedView();
        }
    }

    private long loadLastPlaybackPosition() {
        long position = -1;
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null) {
            try {
                String url = !Helper.isNullOrEmpty(actualClaim.getShortUrl()) ? actualClaim.getShortUrl() : actualClaim.getPermanentUrl();
                String key = String.format("PlayPos_%s", LbryUri.normalize(url));
                Context context = getContext();
                if (context != null) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    position = sp.getLong(key, -1);
                }
            } catch (LbryUriException ex) {
                ex.printStackTrace();
            }
        }
        return position;
    }

    private void savePlaybackPosition() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();
        if (currentPlayer != null && actualClaim != null) {
            try {
                String url = !Helper.isNullOrEmpty(actualClaim.getShortUrl()) ? actualClaim.getShortUrl() : actualClaim.getPermanentUrl();
                String key = String.format("PlayPos_%s", LbryUri.normalize(url));
                long position = currentPlayer.getCurrentPosition();
                Context context = getContext();
                if (context != null) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    sp.edit().putLong(key, position).apply();
                }
            } catch (LbryUriException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String buildMarkdownHtml(String markdown) {
        List<Extension> extensions =  Arrays.asList(
                AutolinkExtension.create(),
                StrikethroughExtension.create(),
                TablesExtension.create(),
                InsExtension.create(),
                TaskListItemsExtension.create()
        );
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .attributeProviderFactory(new AttributeProviderFactory() {
                    @Override
                    public AttributeProvider create(AttributeProviderContext context) {
                        return new CodeAttributeProvider();
                    }
                })
                .build();
        String markdownHtml = renderer.render(document);

        return "<!doctype html>\n" +
                "        <html>\n" +
                "          <head>\n" +
                "            <meta charset=\"utf-8\"/>\n" +
                "            <meta name=\"viewport\" content=\"width=device-width, user-scalable=no\"/>\n" +
                "            <style type=\"text/css\">\n" +
                "              :root { color-scheme: light dark }\n" +
                "              body { font-family: 'Inter', sans-serif; margin: 16px }\n" +
                "              img { width: 100%; }\n" +
                "              pre { white-space: pre-wrap; word-wrap: break-word }\n" +
                "            </style>\n" +
                "          </head>\n" +
                "          <body>\n" +
                "            <div id=\"content\">\n" +
                markdownHtml +
                "            </div>\n" +
                "          </body>\n" +
                "        </html>";
    }

    public void playFirstItemInCollection() {
        if (playlistClaims.size() > 0) {
            playClaimFromCollection(playlistClaims.get(0), 0);
        }
    }

    private void playClaimFromCollection(Claim theClaim, int index) {
        alwaysAutoplay = true;
        collectionClaimItem = theClaim;
        renderClaim();

        // Save to view history, when playing from a collection
        if (!Claim.TYPE_COLLECTION.equalsIgnoreCase(theClaim.getType())) {
            Helper.saveViewHistory(theClaim.getUnifiedUrl(), theClaim);
        }

        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).setCurrentPlaylistIndex(index);
        }

        checkAndLoadChannelSettings();
        checkAndLoadComments(true);
        checkAndLoadRelatedContent();
    }

    private void loadRelatedContent() {
        // reset the list view
        View root = getView();
        Activity a = getActivity();
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null && root != null && a != null) {
            Context context = getContext();

            List<Claim> loadingPlaceholders = new ArrayList<>();
            int loadingPlaceholdersLength = 15;
            for (int i = 0; i < loadingPlaceholdersLength; i++) {
                Claim placeholder = new Claim();
                placeholder.setLoadingPlaceholder(true);
                loadingPlaceholders.add(placeholder);
            }
            relatedContentAdapter = new ClaimListAdapter(loadingPlaceholders, context);
            relatedContentAdapter.setContextGroupId(FILE_CONTEXT_GROUP_ID);

            RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
            relatedContentList.setAdapter(relatedContentAdapter);

            ProgressBar relatedLoading = root.findViewById(R.id.file_view_related_content_progress);
            boolean canShowMatureContent = false;
            if (context != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
            }

            String title = actualClaim.getTitle();
            String claimId = actualClaim.getClaimId();

            final boolean nsfw = canShowMatureContent;
            relatedLoading.setVisibility(View.VISIBLE);

            // Making a request which explicitly uses a certain value form the amount of results needed
            // and no processing any possible exception, so using a callable instead of an AsyncTask
            // makes sense for all Android API Levels
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    LighthouseSearch callable = new LighthouseSearch(title, RELATED_CONTENT_SIZE, 0,
                            nsfw, claimId, null, null, null, null);
                    Future<List<String>> future = ((OdyseeApp) a.getApplication()).getExecutor().submit(callable);

                    try {
                        List<String> urls = future.get();

                        Callable<List<Claim>> resolveCallable = () -> Lbry.resolve(urls, Lbry.API_CONNECTION_STRING);
                        Future<List<Claim>> resolveFuture = ((OdyseeApp) a.getApplication()).getExecutor().submit(resolveCallable);

                        List<Claim> result = resolveFuture.get().stream().filter(c -> c != null).collect(Collectors.toList());
                        if (!urls.contains("")) {
                            urls.add(""); // Explicit empty string as catch-all for LbryUri.normalize errors
                        }
                        Collections.sort(result, Ordering.explicit(urls).onResultOf(claim -> {
                            try {
                                return LbryUri.normalize(claim.getPermanentUrl());
                            } catch (LbryUriException ex) {
                                ex.printStackTrace();
                            }
                            return "";
                        }));

                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                relatedContentRequestSucceeded(result);
                                relatedLoading.setVisibility(View.GONE);
                            }
                        });
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    public void onPlaylistOverlayClaimClicked(Claim claimItem, int position) {
        if ((fileClaim != null && Claim.TYPE_COLLECTION.equalsIgnoreCase(fileClaim.getValueType())) ||
                (claimItem != null && playlistClaims.size() > position)) {
            playClaimFromCollection(claimItem, position);
            return;
        }
    }

    @Override
    public void onClaimClicked(Claim claimItem, int position) {
        if (claimItem.isLoadingPlaceholder()) {
            return;
        }

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.clearCurrentPlaylist();
            collectionClaimItem = null;
            playlistClaims = new ArrayList<>();
            playlistResolved = false;

            activity.openFileUrl(claimItem.getPermanentUrl()); //openClaimUrl(claim.getPermanentUrl());
        }
    }

    // Perform the download
    @Override
    public void onFilePicked(String filePath, Uri intentData) {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(currentMediaSourceOriginalUrl));
            request.setDestinationUri(Uri.fromFile(new File(filePath)));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            request.setAllowedOverMetered(!((MainActivity) activity).downloadWifiOnly());
            request.setAllowedOverRoaming(!((MainActivity) activity).downloadWifiOnly());

            try {
                DocumentsContract.deleteDocument(activity.getContentResolver(), intentData);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }

            DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            currentDownloadId = manager.enqueue(request);

            View root = getView();
            if (root != null) {
                downloadProgressScheduler = Executors.newSingleThreadScheduledExecutor();
                downloadProgressScheduler.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(currentDownloadId);

                        Cursor cursor = manager.query(query);
                        if (cursor.moveToFirst()) {
                            int totalColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                            int downloadedSoFarColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                            int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                            if (totalColumnIndex > -1 && downloadedSoFarColumnIndex > -1 && statusColumnIndex > -1) {
                                int total = cursor.getInt(totalColumnIndex);
                                int downloadedSoFar = cursor.getInt(downloadedSoFarColumnIndex);

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (cursor.getInt(statusColumnIndex)) {
                                            case DownloadManager.STATUS_FAILED:
                                                showError(getString(R.string.unable_to_download_claim));
                                                onDownloadAborted();
                                                downloadProgressScheduler.shutdown();
                                                break;

                                            case DownloadManager.STATUS_RUNNING:
                                                ProgressBar downloadProgressView = root.findViewById(R.id.file_view_download_progress);
                                                downloadProgressView.setProgress((int) (((double) downloadedSoFar / total) * 100));
                                                break;

                                            case DownloadManager.STATUS_SUCCESSFUL:
                                                showMessage(R.string.exo_download_completed);
                                                ((ImageView) root.findViewById(R.id.file_view_action_download_icon)).setImageResource(R.drawable.ic_download);
                                                Helper.setViewVisibility(root.findViewById(R.id.file_view_download_progress), View.GONE);
                                                downloadInProgress = false;
                                                downloadProgressScheduler.shutdown();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }, 0, 1, TimeUnit.SECONDS);

                BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(currentDownloadId);
                        Cursor cursor = manager.query(query);
                        if (cursor.getCount() == 0) { // Download was cancelled
                            onDownloadAborted();
                        }
                    }
                };
                activity.registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }
        }
    }

    @Override
    public void onFilePickerCancelled() {
        onDownloadAborted();
    }

    @MainThread
    private void relatedContentRequestSucceeded(List<Claim> claims) {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        List<Claim> filteredClaims = claims.stream().filter(c -> !c.getClaimId().equalsIgnoreCase(actualClaim.getClaimId()))
                                           .collect(Collectors.toList());

        filteredClaims = Helper.filterClaimsByBlockedChannels(filteredClaims, Lbryio.mutedChannels);

        Context ctx = getContext();
        if (ctx != null) {
            relatedContentAdapter.setItems(filteredClaims);
            relatedContentAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                @Override
                public void onClaimClicked(Claim claim, int position) {
                    if (claim.isLoadingPlaceholder()) {
                        return;
                    }

                    if (ctx instanceof MainActivity) {
                        MainActivity activity = (MainActivity) ctx;
                        activity.clearCurrentPlaylist();
                        collectionClaimItem = null;
                        playlistClaims = new ArrayList<>();
                        playlistResolved = false;

                        if (claim.getName().startsWith("@")) {
                            activity.openChannelClaim(claim);
                        } else {
                            purgeScheduledTasks();
                            Map<String, Object> params = new HashMap<>(1);
                            params.put("url", !Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());

                            setParams(params);
                            checkParams();
                        }
                    }
                }
            });

            View v = getView();
            if (v != null) {
                View root = getView();
                RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
                relatedContentList.setAdapter(relatedContentAdapter);
                relatedContentAdapter.notifyDataSetChanged();

                Helper.setViewVisibility(
                        v.findViewById(R.id.file_view_no_related_content),
                        relatedContentAdapter == null || relatedContentAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            // if related content loads before comments, this will affect the scroll position
            // so just ensure that we are at the correct position
            scrollToCommentHash();
        }
    }

    private void loadComments() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        View root = getView();
        if (root != null && actualClaim != null) {
            ProgressBar commentsLoading = root.findViewById(R.id.file_view_comments_progress);
            CommentListTask task = new CommentListTask(1, 200, actualClaim.getClaimId(), commentsLoading, new CommentListHandler() {
                @Override
                public void onSuccess(List<Comment> comments, boolean hasReachedEnd) {
                    if (!comments.isEmpty()) {
                        // Load and process comments reactions on a different thread so main thread is not blocked
                        Helper.setViewVisibility(commentsLoading, View.VISIBLE);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Map<String, Reactions> commentReactions = loadReactions(comments);
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Helper.setViewVisibility(commentsLoading, View.GONE);
                                            processCommentReactions(comments, commentReactions);
                                        }
                                    });
                                }
                            }
                        }).start();
                    } else {
                        checkNoComments();
                    }
                }

                @Override
                public void onError(Exception error) {
                    if (error != null) {
                        error.printStackTrace();
                    }
                    checkNoComments();
                }

                private void processCommentReactions(List<Comment> comments, Map<String, Reactions> commentReactions) {
                    for (Comment c: comments) {
                        if (commentReactions != null) {
                            c.setReactions(commentReactions.get(c.getId()));
                        } else {
                            c.setReactions(new Reactions(0, 0, false, false));
                        }
                    }

                    List<Comment> rootComments = new ArrayList<>();

                    for (Comment c : comments) {
                        if (c.getParentId() == null) {
                            rootComments.add(c);
                        }
                    }

                    // Now we have level 0 comments to content

                    if (commentReactions != null) {
                        Collections.sort(rootComments, new Comparator<Comment>() {
                            @Override
                            public int compare(Comment o1, Comment o2) {
                                int o1SelfLiked = (Lbryio.isSignedIn() &&  o1.getReactions() != null && o1.getReactions().isLiked()) ? 1 : 0;
                                int o2SelfLiked = (Lbryio.isSignedIn() && o2.getReactions() != null && o2.getReactions().isLiked()) ? 1 : 0;

                                int o1OtherLikes = o1.getReactions() != null ? o1.getReactions().getOthersLikes() : 0;
                                int o2OtherLikes = o2.getReactions() != null ? o2.getReactions().getOthersLikes() : 0;

                                return (o2OtherLikes + o2SelfLiked) - (o1OtherLikes + o1SelfLiked);
                            }
                        });
                    }

                    // Direct comments are now sorted by their amount of likes. We can now pick the
                    // one to be displayed as the collapsed single comment.
                    Comment singleComment = rootComments.get(0);

                    TextView commentText = singleCommentRoot.findViewById(R.id.comment_text);
                    ImageView thumbnailView = singleCommentRoot.findViewById(R.id.comment_thumbnail);
                    View noThumbnailView = singleCommentRoot.findViewById(R.id.comment_no_thumbnail);
                    TextView alphaView = singleCommentRoot.findViewById(R.id.comment_thumbnail_alpha);

                    commentText.setText(singleComment.getText());
                    commentText.setMaxLines(3);
                    commentText.setEllipsize(TextUtils.TruncateAt.END);
                    commentText.setClickable(true);
                    commentText.setTextIsSelectable(false);

                    boolean hasThumbnail = singleComment.getPoster() != null && !Helper.isNullOrEmpty(singleComment.getPoster().getThumbnailUrl());
                    thumbnailView.setVisibility(hasThumbnail ? View.VISIBLE : View.INVISIBLE);
                    noThumbnailView.setVisibility(!hasThumbnail ? View.VISIBLE : View.INVISIBLE);

                    int bgColor = Helper.generateRandomColorForValue(singleComment.getChannelId());
                    Helper.setIconViewBackgroundColor(noThumbnailView, bgColor, false, getContext());
                    if (hasThumbnail) {
                        Context ctx = getContext();
                        if (ctx != null) {
                            Context appCtx = ctx.getApplicationContext();
                            Glide.with(appCtx).asBitmap().load(singleComment.getPoster().getThumbnailUrl()).
                                    apply(RequestOptions.circleCropTransform()).into(thumbnailView);
                        }
                    }
                    alphaView.setText(singleComment.getChannelName() != null ? singleComment.getChannelName().substring(1, 2).toUpperCase() : null);
                    singleCommentRoot.findViewById(R.id.comment_actions_area).setVisibility(View.GONE);
                    singleCommentRoot.findViewById(R.id.comment_time).setVisibility(View.GONE);
                    singleCommentRoot.findViewById(R.id.comment_channel_name).setVisibility(View.GONE);
                    singleCommentRoot.findViewById(R.id.comment_more_options).setVisibility(View.GONE);

                    singleCommentRoot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            expandButton.performClick();
                        }
                    });

                    for (Comment c : comments) {
                        if (!rootComments.contains(c)) {
                            if (c.getParentId() != null) {
                                rootComments.stream().filter(v -> c.getParentId().equalsIgnoreCase(v.getId()))
                                                     .findFirst()
                                                     .ifPresent(item -> rootComments.add(rootComments.indexOf(item) + 1, c));
                            }
                        }
                    }
                    comments = rootComments;

                    Context ctx = getContext();
                    View root = getView();
                    if (ctx != null && root != null) {
                        ensureCommentListAdapterCreated(comments);
                    }
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void ensureCommentListAdapterCreated(final List<Comment> comments) {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if ( commentListAdapter == null ) {
            final Context androidContext = getContext();
            final View root = getView();

            commentListAdapter = new CommentListAdapter(comments, getContext(), actualClaim, new CommentListAdapter.CommentListListener() {
                @Override
                public void onListChanged() {
                    checkNoComments();
                }

                @Override
                public void onCommentReactClicked(Comment c, boolean liked) {
                    if (root != null) {
                        AccountManager am = AccountManager.get(root.getContext());
                        Account odyseeAccount = Helper.getOdyseeAccount(am.getAccounts());
                        if (odyseeAccount != null) {
                            react(c, liked);
                        } else {
                            MainActivity a = (MainActivity) getActivity();
                            if (a != null) {
                                a.driveUserSignIn();
                            }
                        }
                    }
                }

                @Override
                public void onReplyClicked(Comment comment) {
                    setReplyToComment(comment);
                }
            });
            commentListAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                @Override
                public void onClaimClicked(Claim claim, int position) {
                    if (!Helper.isNullOrEmpty(claim.getName()) && claim.getName().startsWith("@") &&
                            androidContext instanceof MainActivity) {
                        removeNotificationAsSource();
                        ((MainActivity) androidContext).openChannelClaim(claim);
                    }
                }
            });

            RecyclerView commentsList = root.findViewById(R.id.file_view_comments_list);
            // Indent reply-type items
            int marginInPx = Math.round(40 * ((float) androidContext.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
            commentsList.addItemDecoration(new CommentItemDecoration(marginInPx));
            commentsList.setAdapter(commentListAdapter);
            commentListAdapter.notifyItemRangeInserted(0, comments.size());

            scrollToCommentHash();
            checkNoComments();
            resolveCommentPosters();
        }
    }

    @MainThread
    private void scrollToCommentHash() {
        if (!Helper.isNullOrEmpty(commentHash)) {
            scrollToComment(commentHash);
        }
    }

    @MainThread
    private void scrollToComment(String hash) {
        View root = getView();
        // check for the position of commentHash if set
        if (root != null && !Helper.isNullOrEmpty(hash) && commentListAdapter != null && commentListAdapter.getItemCount() > 0) {
            RecyclerView commentList = root.findViewById(R.id.file_view_comments_list);
            int position = commentListAdapter.getPositionForComment(hash);
            RecyclerView.LayoutManager listLayoutManager = commentList.getLayoutManager();
            if (position > -1 && listLayoutManager != null) {
                switchCommentListVisibility(true);
                commentListAdapter.switchExpandedStateUI(false);
                commentListAdapter.collapseExceptHash(hash);

                NestedScrollView scrollView = root.findViewById(R.id.file_view_scroll_view);
                scrollView.requestChildFocus(commentList, commentList);
                listLayoutManager.scrollToPosition(position);
            }
        }
    }

    private void checkNoComments() {
        View root = getView();
        if (root != null) {
            Helper.setViewVisibility(root.findViewById(R.id.file_view_no_comments),
                    commentListAdapter == null || commentListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            Helper.setViewVisibility(root.findViewById(R.id.expand_commentarea_button),
                    commentListAdapter == null || commentListAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            Helper.setViewVisibility(root.findViewById(R.id.collapsed_comment),
                    (commentListAdapter != null && !commentListAdapter.getCollapsed()) ||
                    commentListAdapter == null || commentListAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);

            if (commentListAdapter == null)
                root.findViewById(R.id.container_comment_form).setVisibility(View.VISIBLE);
        }
    }

    private void resolveCommentPosters() {
        if (commentListAdapter != null) {
            long st = System.currentTimeMillis();
            List<String> urlsToResolve = new ArrayList<>(commentListAdapter.getClaimUrlsToResolve());
            if (urlsToResolve.size() > 0) {
                ResolveTask task = new ResolveTask(urlsToResolve, Lbry.API_CONNECTION_STRING, null, new ResolveResultHandler() {
                    @Override
                    public void onSuccess(List<Claim> claims) {
                        if (commentListAdapter != null) {
                            for (Claim claim : claims) {
                                if (claim.getClaimId() != null) {
                                    commentListAdapter.updatePosterForComment(claim.getClaimId(), claim);
                                }
                            }

                            // filter for blocked comments
                            commentListAdapter.filterBlockedChannels(Lbryio.mutedChannels);

                            commentListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        // pass
                    }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isInFullscreenMode()) {
            disableFullScreenMode();
            return true;
        }

        if (isImageViewerVisible()) {
            View root = getView();
            if (root != null) {
                root.findViewById(R.id.file_view_imageviewer_container).setVisibility(View.GONE);
            }
            restoreMainActionButton();
            return true;
        }
        if (isWebViewVisible()) {
            View root = getView();
            if (root != null) {
                root.findViewById(R.id.file_view_webview_container).setVisibility(View.GONE);
                ((RelativeLayout) root.findViewById(R.id.file_view_webview_container)).removeAllViews();
            }
            closeWebView();
            restoreMainActionButton();
            return true;
        }
        if (isPlaylistOverlayMaximized()) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                ((MainActivity) context).minimizePlaylistOverlay();
            }
            return true;
        }

        return false;
    }

    private boolean isPlaylistOverlayMaximized() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            return ((MainActivity) context).getPlaylistOverlayState() == MainActivity.PLAYLIST_OVERLAY_STATE_MAXIMIZED;
        }
        return false;
    }

    private boolean isImageViewerVisible() {
        View view = getView();
        return view != null && view.findViewById(R.id.file_view_imageviewer_container).getVisibility() == View.VISIBLE;
    }

    private boolean isWebViewVisible() {
        View view = getView();
        return view != null && view.findViewById(R.id.file_view_webview_container).getVisibility() == View.VISIBLE;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void enableFullScreenMode() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            View root = getView();
            if (root != null) {
                ConstraintLayout globalLayout = root.findViewById(R.id.file_view_global_layout);
                View exoplayerContainer = root.findViewById(R.id.file_view_exoplayer_container);
                ((ViewGroup) exoplayerContainer.getParent()).removeView(exoplayerContainer);
                globalLayout.addView(exoplayerContainer);

                MainActivity activity = (MainActivity) context;
                activity.enterFullScreenMode();

                exoplayerContainer.setPadding(0, 0, 0, 0);
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void disableFullScreenMode() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            View root = getView();
            if (root != null) {
                RelativeLayout mediaContainer = root.findViewById(R.id.file_view_media_container);
                View exoplayerContainer = root.findViewById(R.id.file_view_exoplayer_container);
                ((ViewGroup) exoplayerContainer.getParent()).removeView(exoplayerContainer);
                mediaContainer.addView(exoplayerContainer);

                exoplayerContainer.setPadding(0, 0, 0, 0);

                activity.exitFullScreenMode();
            }
        }
    }

    private boolean isInFullscreenMode() {
        View view = getView();
        if (view != null) {
            View exoplayerContainer = view.findViewById(R.id.file_view_exoplayer_container);
            return exoplayerContainer.getParent() instanceof ConstraintLayout;
        }
        return false;
    }

    private void scheduleElapsedPlayback() {
        Activity a = getActivity();
        if (!elapsedPlaybackScheduled && a != null) {
            futureElapsedPlayback = ((OdyseeApp) a.getApplication()).getScheduledExecutor().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();
                                if (currentPlayer != null) {
                                    elapsedDuration = currentPlayer.getCurrentPosition();
                                    int elapsedSeconds = Double.valueOf(elapsedDuration / 1000.0).intValue();
                                    if (elapsedDuration > 0 && elapsedSeconds % 5 == 0 && elapsedSeconds != lastPositionSaved) {
                                        // save playback position every 5 seconds
                                        savePlaybackPosition();
                                        lastPositionSaved = elapsedSeconds;
                                    }

                                }
                            }
                        });
                    }
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
            elapsedPlaybackScheduled = true;
        }
    }

    private void loadAndScheduleDurations() {
        Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();
        if (currentPlayer != null && playbackStarted) {
            elapsedDuration = currentPlayer.getCurrentPosition() < 0 ? 0 : currentPlayer.getCurrentPosition();

            scheduleElapsedPlayback();
        }
    }

    private void logPlay(String url, long startTimeMillis) {
        long timeToStartMillis = startTimeMillis > 0 ? System.currentTimeMillis() - startTimeMillis : 0;

        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        bundle.putLong("time_to_start_ms", timeToStartMillis);
        bundle.putLong("time_to_start_seconds", Double.valueOf(timeToStartMillis / 1000.0).longValue());
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_PLAY, bundle);

        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        logFileView(actualClaim.getPermanentUrl(), timeToStartMillis);
    }

    private void logFileView(String url, long timeToStart) {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null) {
            String authToken = Lbryio.AUTH_TOKEN;
            Map<String, String> options = new HashMap<>();
            options.put("uri", url);
            options.put("claim_id", actualClaim.getClaimId());
            options.put("outpoint", String.format("%s:%d", actualClaim.getTxid(), actualClaim.getNout()));
            if (timeToStart > 0) {
                options.put("time_to_start", String.valueOf(timeToStart));
            }
            if (!Helper.isNullOrEmpty(authToken)) {
                options.put("auth_token", authToken);
            }

            Activity activity = getActivity();
            Supplier<Boolean> s = new Supplier<Boolean>() {
                @Override
                public Boolean get() {
                    try {
                        Lbryio.call("file", "view", options,  null).close();
                        return true;
                    } catch (LbryioRequestException | LbryioResponseException ex) {
                        return false;
                    }
                }
            };

            CompletableFuture<Boolean> cf = CompletableFuture.supplyAsync(s);
            cf.whenComplete((result, ex) -> {
                if (result) {
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                claimEligibleRewards();
                            }
                        });
                    }
                    if (ex != null) {
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showError(ex.getMessage());
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private void checkIsFollowing() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null && actualClaim.getSigningChannel() != null) {
            boolean isFollowing = Lbryio.isFollowing(actualClaim.getSigningChannel());
            boolean notificationsDisabled = Lbryio.isNotificationsDisabled(actualClaim.getSigningChannel());
            Context context = getContext();
            View root = getView();
            if (context != null && root != null) {
                View iconFollow = root.findViewById(R.id.file_view_icon_follow);
                SolidIconView iconUnfollow = root.findViewById(R.id.file_view_icon_unfollow);
                SolidIconView iconBell = root.findViewById(R.id.file_view_icon_bell);
                Helper.setViewVisibility(iconFollow, !isFollowing ? View.VISIBLE: View.GONE);
                Helper.setViewVisibility(iconUnfollow, isFollowing ? View.VISIBLE : View.GONE);
                Helper.setViewVisibility(iconBell, isFollowing ? View.VISIBLE : View.GONE);

                iconBell.setText(notificationsDisabled ? R.string.fa_bell : R.string.fa_bell_slash);
            }
        }
    }

    private void claimEligibleRewards() {
        // attempt to claim eligible rewards after viewing or playing a file (fail silently)
        final String authToken = Lbryio.AUTH_TOKEN;
        ClaimRewardTask firstStreamTask = new ClaimRewardTask(Reward.TYPE_FIRST_STREAM, null, authToken, eligibleRewardHandler);
        ClaimRewardTask dailyViewTask = new ClaimRewardTask(Reward.TYPE_DAILY_VIEW, null, authToken, eligibleRewardHandler);
        firstStreamTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        dailyViewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private final ClaimRewardTask.ClaimRewardHandler eligibleRewardHandler = new ClaimRewardTask.ClaimRewardHandler() {
        @Override
        public void onSuccess(double amountClaimed, String message) {
            if (Helper.isNullOrEmpty(message)) {
                showMessage(getResources().getQuantityString(
                        R.plurals.claim_reward_message,
                        amountClaimed == 1 ? 1 : 2,
                        new DecimalFormat(Helper.LBC_CURRENCY_FORMAT_PATTERN).format(amountClaimed)));
            }
        }

        @Override
        public void onError(Exception error) {
            // pass
        }
    };

    private void onDownloadAborted() {
        downloadInProgress = false;

        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim != null) {
            actualClaim.setFile(null);
        }
        View root = getView();
        if (root != null) {
            ((ImageView) root.findViewById(R.id.file_view_action_download_icon)).setImageResource(R.drawable.ic_download);
            Helper.setViewVisibility(root.findViewById(R.id.file_view_download_progress), View.GONE);
        }

        restoreMainActionButton();
    }

    private void restoreMainActionButton() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.file_view_main_action_loading).setVisibility(View.INVISIBLE);
            root.findViewById(R.id.file_view_main_action_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClaimsFetched(List<Claim> claims) {
        checkOwnClaim();
    }

    @Override
    public void onPortraitOrientationEntered() {
        Context context = getContext();
        if (context instanceof MainActivity && ((MainActivity) context).isInFullscreenMode()) {
            disableFullScreenMode();
        }
    }

    @Override
    public void onLandscapeOrientationEntered() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            if (activity.isEnteringPIPMode() || activity.isInPictureInPictureMode()) {
                return;
            }
            Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
            if (actualClaim != null && actualClaim.isPlayable() && !activity.isInFullscreenMode()) {
                enableFullScreenMode();
            }
        }
    }

    private void checkRewardsDriver() {
        Context ctx = getContext();
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (ctx != null && actualClaim != null && !actualClaim.isFree() && actualClaim.getFile() == null) {
            String rewardsDriverText = getString(R.string.earn_some_credits_to_access);
            checkRewardsDriverCard(rewardsDriverText, actualClaim.getActualCost(Lbryio.LBCUSDRate).doubleValue());
        }
    }

    private static class LbryWebViewClient extends WebViewClient {
        private final Context context;
        public LbryWebViewClient(Context context) {
            this.context = context;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (context != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, url);
                context.startActivity(intent);
            }
            return true;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == FILE_CONTEXT_GROUP_ID && (item.getItemId() == R.id.action_block || item.getItemId() == R.id.action_mute)) {
            if (relatedContentAdapter != null) {
                int position = relatedContentAdapter.getCurrentPosition();
                Claim claim = relatedContentAdapter.getItems().get(position);
                if (claim != null && claim.getSigningChannel() != null) {
                    Claim channel = claim.getSigningChannel();
                    Context context = getContext();
                    MainActivity activity = (MainActivity) context;
                    if (item.getItemId() == R.id.action_block) {
                        activity.handleBlockChannel(channel, null);
                    } else {
                        activity.handleMuteChannel(channel);
                    }
                }
            }
            return true;
        }

        if (item.getGroupId() == FILE_CONTEXT_GROUP_ID && item.getItemId() == R.id.action_report) {
            if (relatedContentAdapter != null) {
                int position = relatedContentAdapter.getCurrentPosition();
                Claim claim = relatedContentAdapter.getItems().get(position);
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).handleReportClaim(claim);
                }
            }
            return true;
        }

        if (item.getGroupId() ==  FILE_CONTEXT_GROUP_ID)  {
            if (relatedContentAdapter != null) {
                int position = relatedContentAdapter.getCurrentPosition();
                Claim claim = relatedContentAdapter.getItems().get(position);
                String url = claim.getPermanentUrl();

                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    if (item.getItemId() == R.id.action_add_to_watch_later) {
                        activity.handleAddUrlToList(url, OdyseeCollection.BUILT_IN_ID_WATCHLATER);
                    } else if (item.getItemId() == R.id.action_add_to_favorites) {
                        activity.handleAddUrlToList(url, OdyseeCollection.BUILT_IN_ID_FAVORITES);
                    } else if (item.getItemId() == R.id.action_add_to_lists) {
                        activity.handleAddUrlToList(url, null);
                    } else if (item.getItemId() == R.id.action_add_to_queue) {
                        activity.handleAddToNowPlayingQueue(claim);
                    }
                }
            }
        }

        Player currentPlayer = MainActivity.playerManager.getCurrentPlayer();
        if (item.getGroupId() == Helper.PLAYBACK_SPEEDS_GROUP_ID) {
            int speed = item.getItemId();
            if (currentPlayer != null) {
                setPlaybackSpeed(currentPlayer, speed);
                return true;
            }
        } else if (item.getGroupId() == Helper.QUALITIES_GROUP_ID) {
            loadingQualityChanged = true;
            int quality = item.getItemId();
            MainActivity activity = (MainActivity) getActivity();

            if (currentPlayer != null && activity != null) {
                activity.setPlayerQuality(currentPlayer, quality);

                updateQualityView(getView());
                return true;
            }
        }
        return false;
    }

    private void checkOwnClaim() {
        Claim claimToCheck = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (claimToCheck != null) {
            boolean isOwnClaim = Lbry.ownClaims.contains(claimToCheck);
            boolean downloadDisabled = claimToCheck.getTags().contains("c:disabled-download");
            View root = getView();
            if (root != null) {
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_report), isOwnClaim ? View.GONE : View.VISIBLE);
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_edit), isOwnClaim ? View.VISIBLE : View.GONE);
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_delete), isOwnClaim ? View.VISIBLE : View.GONE);

                // Also check if downloads are disabled
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_download), downloadDisabled ? View.GONE : View.VISIBLE);
            }
        }
    }

    public void onEnterPIPMode() {
        View root = getView();
        if (root != null) {
            StyledPlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
            playerView.setVisibility(View.GONE);
        }
    }

    public void onExitPIPMode() {
        View root = getView();
        if (root != null) {
            StyledPlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
            playerView.setVisibility(View.VISIBLE);
        }
    }

    private void showStoragePermissionRefusedError() {
        View root = getView();
        if (root != null) {
            showError(getString(R.string.storage_permission_rationale_download));
        }
    }

    private void fetchChannels() {
        if (Lbry.ownChannels != null && !Lbry.ownChannels.isEmpty()) {
            updateChannelList(Lbry.ownChannels);
            return;
        }

        fetchingChannels = true;
        disableChannelSpinner();
        Map<String, Object> options = Lbry.buildClaimListOptions(Claim.TYPE_CHANNEL, 1, 999, true);
        ClaimListTask task = new ClaimListTask(options, progressLoadingChannels, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                Lbry.ownChannels = new ArrayList<>(claims);
                updateChannelList(Lbry.ownChannels);
                enableChannelSpinner();
                fetchingChannels = false;
            }

            @Override
            public void onError(Exception error) {
                enableChannelSpinner();
                fetchingChannels = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void disableChannelSpinner() {
        Helper.setViewEnabled(commentChannelSpinner, false);
    }
    private void enableChannelSpinner() {
        Helper.setViewEnabled(commentChannelSpinner, true);
        if (commentChannelSpinner != null) {
            Claim selectedClaim = (Claim) commentChannelSpinner.getSelectedItem();
            if (selectedClaim != null) {
                if (selectedClaim.isPlaceholder()) {
                    showChannelCreator();
                }
            }
        }
    }
    private void showChannelCreator() {
        MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            activity.showChannelCreator(this);
        }
    }

    @Override
    public void onChannelCreated(Claim claimResult) {
        // add the claim to the channel list and set it as the selected item
        if (commentChannelSpinnerAdapter != null) {
            commentChannelSpinnerAdapter.add(claimResult);
        } else {
            updateChannelList(Collections.singletonList(claimResult));
        }
        if (commentChannelSpinner != null && commentChannelSpinnerAdapter != null) {
            // Ensure adapter is set for the spinner
            if (commentChannelSpinner.getAdapter() == null) {
                commentChannelSpinner.setAdapter(commentChannelSpinnerAdapter);
            }

            int adapterCount = commentChannelSpinnerAdapter.getCount();

            if (adapterCount == 1) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.saveSharedUserState();
                }
            }
            commentChannelSpinner.setSelection(adapterCount - 1);
        }

        if (commentChannelSpinner != null) {
            View formRoot = (View) commentChannelSpinner.getParent().getParent();
            formRoot.setVisibility(View.VISIBLE);
            formRoot.findViewById(R.id.has_channels).setVisibility(View.VISIBLE);
            formRoot.findViewById(R.id.no_channels).setVisibility(View.GONE);
        }
    }

    private void updateChannelList(List<Claim> channels) {
        Context context = getContext();
        if (commentChannelSpinnerAdapter == null) {
            if (context != null) {
                commentChannelSpinnerAdapter = new InlineChannelSpinnerAdapter(context, R.layout.spinner_item_channel, new ArrayList<>(channels));
                commentChannelSpinnerAdapter.addPlaceholder(false);
                commentChannelSpinnerAdapter.notifyDataSetChanged();
            }
        } else {
            commentChannelSpinnerAdapter.clear();
            commentChannelSpinnerAdapter.addAll(channels);
            commentChannelSpinnerAdapter.addPlaceholder(false);
            commentChannelSpinnerAdapter.notifyDataSetChanged();
        }

        if (commentChannelSpinner != null) {
            commentChannelSpinner.setAdapter(commentChannelSpinnerAdapter);
        }

        if (commentChannelSpinnerAdapter != null && commentChannelSpinner != null) {
            if (commentChannelSpinnerAdapter.getCount() > 1) {
                String defaultChannelName = Helper.getDefaultChannelName(context);
                List<Claim> defaultChannel = channels.stream().filter(c -> c != null && c.getName().equalsIgnoreCase(defaultChannelName)).collect(Collectors.toList());

                if (defaultChannel.size() > 0) {
                    commentChannelSpinner.setSelection(commentChannelSpinnerAdapter.getItemPosition(defaultChannel.get(0)));
                } else {
                    commentChannelSpinner.setSelection(1);
                }
            }
        }
    }

    private void initCommentForm(View root) {
        MainActivity activity = (MainActivity) getActivity();

        if (activity != null) {
            activity.refreshChannelCreationRequired(root);
        }

        textCommentLimit.setText(String.format("%d / %d", Helper.getValue(inputComment.getText()).length(), Comment.MAX_LENGTH));

        buttonClearReplyToComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearReplyToComment();
            }
        });

        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndCheckPostComment();
            }
        });

        buttonCreateChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fetchingChannels) {
                    showChannelCreator();
                }
            }
        });

        buttonCommentSignedInUserRequired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity activity = (MainActivity) getActivity();

                if (activity != null) {
                    activity.simpleSignIn(0);
                }
            }
        });

        inputComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int len = charSequence.length();
                textCommentLimit.setText(String.format("%d / %d", len, Comment.MAX_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        commentChannelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item instanceof Claim) {
                    Claim claim = (Claim) item;
                    if (claim.isPlaceholder()) {
                        if (!fetchingChannels) {
                            showChannelCreator();
                            if (commentChannelSpinnerAdapter.getCount() > 1) {
                                commentChannelSpinner.setSelection(commentChannelSpinnerAdapter.getCount() - 1);
                            }
                        }
                    } else {
                        updatePostAsChannel(claim);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void validateAndCheckPostComment() {
        String comment = Helper.getValue(inputComment.getText());
        Claim channel = (Claim) commentChannelSpinner.getSelectedItem();

        if (Helper.isNullOrEmpty(comment)) {
            showError(getString(R.string.please_enter_comment));
            return;
        }
        if (channel == null || Helper.isNullOrEmpty(channel.getClaimId())) {
            showError(getString(R.string.please_select_channel));
            return;
        }

        if (currentCreatorSetting != null && currentCreatorSetting.getMinTipAmountCommentValue() > 0) {
            // TODO: We need to add back a field to the post comment form for the user to
            // input the amount of credits to send when posting a comment.
            // For now, this will always fail.
            showError(getString(R.string.please_enter_min_tip_amount));
            return;
        }

        postComment();
    }

    private void updatePostAsChannel(Claim channel) {
        boolean hasThumbnail = !Helper.isNullOrEmpty(channel.getThumbnailUrl());
        Helper.setViewVisibility(commentPostAsThumbnail, hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        Helper.setViewVisibility(commentPostAsNoThumbnail, !hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        Helper.setViewText(commentPostAsAlpha, channel.getName() != null ? channel.getName().substring(1, 2).toUpperCase() : null);

        Context context = getContext();
        int bgColor = Helper.generateRandomColorForValue(channel.getClaimId());
        Helper.setIconViewBackgroundColor(commentPostAsNoThumbnail, bgColor, false, context);

        if (hasThumbnail && context != null) {
            Glide.with(context.getApplicationContext()).
                    asBitmap().
                    load(channel.getThumbnailUrl(Utils.CHANNEL_THUMBNAIL_WIDTH, Utils.CHANNEL_THUMBNAIL_HEIGHT, Utils.CHANNEL_THUMBNAIL_Q)).
                    apply(RequestOptions.circleCropTransform()).
                    into(commentPostAsThumbnail);
        }
    }

    private void beforePostComment() {
        postingComment = true;
        Helper.setViewEnabled(commentChannelSpinner, false);
        Helper.setViewEnabled(inputComment, false);
        Helper.setViewEnabled(buttonClearReplyToComment, false);
        Helper.setViewEnabled(buttonPostComment, false);
    }

    private void afterPostComment() {
        Helper.setViewEnabled(commentChannelSpinner, true);
        Helper.setViewEnabled(inputComment, true);
        Helper.setViewEnabled(buttonClearReplyToComment, true);
        Helper.setViewEnabled(buttonPostComment, true);
        postingComment = false;
    }

    private Comment buildPostComment() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        Comment comment = new Comment();
        Claim channel = (Claim) commentChannelSpinner.getSelectedItem();
        comment.setClaimId(actualClaim.getClaimId());
        comment.setChannelId(channel.getClaimId());
        comment.setChannelName(channel.getName());
        comment.setText(Helper.getValue(inputComment.getText()));
        comment.setPoster(channel);
        if (replyToComment != null) {
            comment.setParentId(replyToComment.getId());
        }

        return comment;
    }

    private void setReplyToComment(Comment comment) {
        replyToComment = comment;
        Helper.setViewText(textReplyingTo, getString(R.string.replying_to, comment.getChannelName()));
        Helper.setViewText(textReplyToBody, comment.getText());
        Helper.setViewVisibility(containerReplyToComment, View.VISIBLE);

        inputComment.requestFocus();
        Context context = getContext();
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputComment, InputMethodManager.SHOW_FORCED);
        }
    }

    private void clearReplyToComment() {
        Helper.setViewText(textReplyingTo, null);
        Helper.setViewText(textReplyToBody, null);
        Helper.setViewVisibility(containerReplyToComment, View.GONE);
        replyToComment = null;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void switchCommentListVisibility(Boolean expanded) {
        View root = getView();
        View relatedContentArea = root.findViewById(R.id.file_view_related_content_area);
        View actionsArea = root.findViewById(R.id.file_view_actions_area);
        View publisherArea = root.findViewById(R.id.file_view_publisher_area);
        ImageButton expandButton = root.findViewById(R.id.expand_commentarea_button);
        Context context = getContext();

        if (expanded) {
            root.findViewById(R.id.collapsed_comment).setVisibility(View.GONE);
            Helper.setViewVisibility(containerCommentForm, View.VISIBLE);
            Helper.setViewVisibility(relatedContentArea, View.GONE);
            Helper.setViewVisibility(actionsArea, View.GONE);
            Helper.setViewVisibility(publisherArea, View.GONE);
            if (context != null) {
                expandButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_close, context.getTheme()));
            }
        } else {
            Helper.setViewVisibility(containerCommentForm, View.GONE);
            root.findViewById(R.id.collapsed_comment).setVisibility(View.VISIBLE);
            Helper.setViewVisibility(relatedContentArea, View.VISIBLE);
            Helper.setViewVisibility(actionsArea, View.VISIBLE);
            Helper.setViewVisibility(publisherArea, View.VISIBLE);
            if (context != null) {
                expandButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand, context.getTheme()));
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
                }
            }
        }
    }

    private void postComment() {
        if (postingComment) {
            return;
        }

        Comment comment = buildPostComment();

        beforePostComment();
        AccountManager am = AccountManager.get(getContext());
        Account odyseeAccount = Helper.getOdyseeAccount(am.getAccounts());

        final View rootView = getView();
        CommentCreateTask task = new CommentCreateTask(comment, am.peekAuthToken(odyseeAccount, "auth_token_type"), progressPostComment, new CommentCreateTask.CommentCreateWithTipHandler() {
            @Override
            public void onSuccess(Comment createdComment) {
                inputComment.setText(null);
                clearReplyToComment();

                final boolean thisIsFirstComment = commentListAdapter == null;

                ensureCommentListAdapterCreated(new ArrayList<Comment>());

                if (commentListAdapter != null) {
                    createdComment.setPoster(comment.getPoster());
                    if (!Helper.isNullOrEmpty(createdComment.getParentId())) {
                        commentListAdapter.addReply(createdComment);
                    } else {
                        commentListAdapter.insert(0, createdComment);
                    }
                }
                afterPostComment();
                checkNoComments();

                if ( thisIsFirstComment ) {
                    expandButton.performClick();
                }

                singleCommentRoot.setVisibility(View.GONE);

                Bundle bundle = new Bundle();
                bundle.putString("claim_id", fileClaim != null ? fileClaim.getClaimId() : null);
                bundle.putString("claim_name", fileClaim != null ? fileClaim.getName() : null);
                LbryAnalytics.logEvent(LbryAnalytics.EVENT_COMMENT_CREATE, bundle);

                showMessage(R.string.comment_posted);
            }

            @Override
            public void onError(Exception error) {
                try {
                    showError(error != null ? error.getMessage() : getString(R.string.comment_error));
                } catch (IllegalStateException ex) {
                    // pass
                }
                afterPostComment();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void react(Comment comment, boolean like) {
        JSONObject options = new JSONObject();
        try {
            options.put("comment_ids", comment.getId());
            options.put("type", like ? "like" : "dislike");
            options.put("clear_types", like ? "dislike" : "like");

            /**
             * This covers the case of a fresh comment being made and added to the list, and then liked by
             * the commenter themself, but the {@link Reactions} field is not instantiated in this case. It
             * would perhaps be better to fix this upstream and make this situation impossible, but even then
             * this last line of defense doesn't hurt.
             */
            if (comment.getReactions() == null) {
                comment.setReactions(Reactions.newInstanceWithNoLikesOrDislikes());
            }

            if ((like && comment.getReactions().isLiked()) || (!like && comment.getReactions().isDisliked())) {
                options.put("remove", true);
            }

            AccountManager am = AccountManager.get(getContext());
            Account odyseeAccount = Helper.getOdyseeAccount(am.getAccounts());
            if (odyseeAccount != null) {
                options.put("auth_token", am.peekAuthToken(odyseeAccount, "auth_token_type"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Activity a = getActivity();
        if (a != null) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // This makes a network connection, so it needs to be executed on a different thread than main.
                    if (Lbry.ownChannels.size() > 0) {
                        try {
                            // This makes a network connection, so it needs to be executed on a different thread than main.
                            Callable<JSONObject> optionsCallable = new BuildCommentReactOptions(options);
                            Future<JSONObject> optionsFuture = ((OdyseeApp) a.getApplication()).getExecutor().submit(optionsCallable);

                            JSONObject opt = optionsFuture.get();

                            Future<?> futureReactions = ((OdyseeApp) a.getApplication()).getExecutor().submit(new ReactToComment(opt));
                            futureReactions.get();

                            refreshCommentAfterReacting(comment);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
    }

    private void refreshCommentAfterReacting(Comment comment) {
        Map<String, Reactions> reactionsList = loadReactions(Collections.singletonList(comment));

        if (reactionsList != null && reactionsList.containsKey(comment.getId())){
            Activity activity = getActivity();

            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commentListAdapter.updateReactions(comment, reactionsList.get(comment.getId()));
                    }
                });
            }
        }
    }

    private void react(Claim claim, boolean like) {
        if (claim == null) {
            return;
        }
        Runnable runnable = () -> {
            if (futureReactions != null && futureReactions.isCancelled()) {
                futureReactions.cancel(true);
            }

            reactions.setLiked(like && !reactions.isLiked());
            reactions.setDisliked(!like && !reactions.isDisliked());
            updateContentReactions();

            Map<String, String> options = new HashMap<>();
            options.put("claim_ids", claim.getClaimId());
            options.put("type", like ? "like" : "dislike");
            options.put("clear_types", like ? "dislike" : "like");

            if ((like && claim.isLiked()) || (!like && claim.isDisliked())) {
                options.put("remove", "true");
            }

            try {
                Lbryio.call("reaction", "react", options, Helper.METHOD_POST, getContext());
            } catch (LbryioRequestException | LbryioResponseException e) {
                e.printStackTrace();

                // Reset reactions to original values on error
                reactions.setLiked(like && !reactions.isLiked());
                reactions.setDisliked(!like && !reactions.isDisliked());
                updateContentReactions();
            } finally {
                loadReactions(claim);
            }
        };

        try {
            new Thread(runnable).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void applyFilterForMutedChannels(List<LbryUri> blockedChannels) {
        if (relatedContentAdapter != null) {
            relatedContentAdapter.filterBlockedChannels(blockedChannels);
        }
        if (commentListAdapter != null) {
            commentListAdapter.filterBlockedChannels(blockedChannels);
        }
    }

    private boolean initialChatLoaded;
    private ChatMessageListAdapter chatMessageListAdapter;

    private void initLivestreamChat() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        // load up the previous 75 chat messages, if there are any
        CommentListTask task = new CommentListTask(1, 75, actualClaim.getClaimId(), null, new CommentListHandler() {
            @Override
            public void onSuccess(List<Comment> comments, boolean hasReachedEnd) {
                initialChatLoaded = true;
                Collections.reverse(comments);
                for (Comment comment : comments) {
                    comment.setHandler(chatMemberClickHandler);
                }

                Context context = getContext();
                if (context != null) {
                    chatMessageListAdapter = new ChatMessageListAdapter(comments, context);
                    if (actualClaim.getSigningChannel() != null) {
                        chatMessageListAdapter.setStreamerClaimId(actualClaim.getSigningChannel().getClaimId());
                    }
                    chatMessageList.setAdapter(chatMessageListAdapter);
                    chatMessageList.scrollToPosition(chatMessageListAdapter.getItemCount() - 1);

                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                    boolean forceDisplay = sp.getBoolean("com.odysee.app.force_display_chat_messages", false);
                    chatMessageListAdapter.setDisplayMessages(forceDisplay);
                }

                checkWebSocketClient();
            }

            @Override
            public void onError(Exception error) {
                // pass
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SneakyThrows
    private void checkWebSocketClient() {
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if ((webSocketClient == null || webSocketClient.isClosed()) && actualClaim != null && !actualClaim.hasSource()) {
            if (actualClaim.getSigningChannel().getCanonicalUrl() != null) {
                String signingChannelShort = String.valueOf(LbryUri.parse(actualClaim.getSigningChannel().getCanonicalUrl()).getClaimId());
                String livechatUrl = String.format("%s%s", Lbryio.WS_COMMENT_BASE_URL, actualClaim.getClaimId());
                livechatUrl = livechatUrl.concat("&category=").concat(actualClaim.getSigningChannel().getNormalizedName())
                        .concat(":").concat(signingChannelShort)
                        .concat("&sub_category=viewer");
                webSocketClient = new WebSocketClient(new URI(livechatUrl)) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                    }

                    @Override
                    public void onMessage(String message) {
                        try {
                            JSONObject json = new JSONObject(message);
                            String type = Helper.getJSONString("type", null, json);

                            if ("delta".equalsIgnoreCase(type) || "viewers".equalsIgnoreCase(type) || "livestream".equalsIgnoreCase(type)) {
                                JSONObject data = Helper.getJSONObject("data", json);

                                Activity a = getActivity();
                                if (data != null && "delta".equalsIgnoreCase(type)) {
                                    JSONObject commentJson = Helper.getJSONObject("comment", data);
                                    if (commentJson != null) {
                                        Comment comment = new Comment();
                                        comment.setHandler(chatMemberClickHandler);
                                        if (a != null) {
                                            a.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    comment.setText(Helper.getJSONString("comment", "", commentJson));
                                                    comment.setChannelId(Helper.getJSONString("channel_id", "", commentJson));
                                                    comment.setChannelName(Helper.getJSONString("channel_name", "", commentJson));
                                                    if (!Helper.isNullOrEmpty(comment.getChannelName())) {
                                                        if (chatMessageListAdapter == null) {
                                                            chatMessageListAdapter = new ChatMessageListAdapter(Collections.singletonList(comment), getContext());
                                                            if (actualClaim.getSigningChannel() != null) {
                                                                chatMessageListAdapter.setStreamerClaimId(actualClaim.getSigningChannel().getClaimId());
                                                            }
                                                            chatMessageList.setAdapter(chatMessageListAdapter);
                                                        } else {
                                                            final boolean wasAtBottom = isChatMessageListAtBottom();
                                                            chatMessageListAdapter.addMessage(comment);
                                                            if (wasAtBottom) {
                                                                // only scroll to the end if the scrollview was previously at the bottom
                                                                ((OdyseeApp) a.getApplication()).getScheduledExecutor().schedule(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        smoothScrollToLastChatMessage();
                                                                    }
                                                                }, 100, TimeUnit.MILLISECONDS);
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                } else if (data != null && "viewers".equalsIgnoreCase(type)) {
                                    int connectedViewers = data.getInt("connected");

                                    if (a != null) {
                                        a.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Context context = a.getApplicationContext();
                                                    String displayText = context.getResources().getString(R.string.livestream_view_count, String.valueOf(connectedViewers));
                                                    View root = getView();
                                                    if (root != null) {
                                                        TextView textViewCount = root.findViewById(R.id.file_view_view_count);
                                                        Helper.setViewText(textViewCount, displayText);
                                                        Helper.setViewVisibility(textViewCount, View.VISIBLE);
                                                    }
                                                    if (livestreamStartingMillis != 0) {
                                                        updatePublishTime(null, null);
                                                    } else { // Broadcast has not started or has finished
                                                        updatePublishTime((Claim.StreamMetadata) actualClaim.getValue(), actualClaim);
                                                    }
                                                } catch (IllegalStateException ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                } else if (data != null && "livestream".equalsIgnoreCase(type)) {
                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
                                    String liveTime = data.getString("live_time");
                                    String endTime = data.getString("end_time");

                                    if (a != null) {
                                        ZonedDateTime zonedEnd = ZonedDateTime.parse(endTime, dtf);
                                        ZonedDateTime zonedStart = ZonedDateTime.parse(liveTime, dtf);

                                        if (zonedEnd != null && zonedStart != null) {
                                            OdyseeApp app = (OdyseeApp) a.getApplication();

                                            Date timeNow = new Date();

                                            long millisecondsEnd = zonedEnd.toInstant().toEpochMilli();
                                            long deltaEnd = millisecondsEnd - timeNow.getTime();

                                            if (deltaEnd > 0) {
                                                scheduledStopPlaying = app.getScheduledExecutor().schedule(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        a.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                MainActivity.playerManager.getCurrentPlayer().stop();
                                                                livestreamStartingMillis = 0;
                                                                renderPublisherNotBroadcasting(actualClaim);
                                                            }
                                                        });
                                                    }
                                                }, deltaEnd, TimeUnit.MILLISECONDS);
                                            }

                                            long millisecondsStart = zonedStart.toInstant().toEpochMilli();
                                            livestreamStartingMillis = millisecondsStart;
                                            updatePublishTime(null, null);
                                            long deltaStart = millisecondsStart - timeNow.getTime();

                                            if (deltaStart > 0) {
                                                scheduledStartPlaying = app.getScheduledExecutor().schedule(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        a.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                JSONObject jsonResult = jsonData.get(fileClaim.getSigningChannel().getClaimId());
                                                                if (jsonResult != null && jsonResult.has("VideoURL")) {
                                                                    try {
                                                                        claimLivestreamUrl = jsonResult.getString("VideoURL");
                                                                        renderPublisherBroadcasting();
                                                                        playMedia();
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }, deltaStart, TimeUnit.MILLISECONDS);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        Context context = getContext();
                        if (context instanceof MainActivity) {
                            MainActivity activity = (MainActivity) context;
                            if (!activity.isShuttingDown() && !leavingFileView) {
                                // attempt to re-establish the connection if the app isn't being closed
                                checkWebSocketClient();
                            }
                        }
                    }

                    @Override
                    public void onError(Exception ex) {
                    }

                    protected void onSetSSLParameters(SSLParameters sslParameters) {
                        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
                    }
                };
                webSocketClient.connect();
            } else {
                String url = actualClaim.getSigningChannel().getPermanentUrl();
                ResolveTask task = new ResolveTask(url, Lbry.API_CONNECTION_STRING, null, new ResolveResultHandler() {
                    @Override
                    public void onSuccess(List<Claim> claims) {
                        actualClaim.setSigningChannel(claims.get(0));
                        checkWebSocketClient();
                    }

                    @Override
                    public void onError(Exception error) { }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private boolean isChatMessageListAtBottom() {
        LinearLayoutManager llm = (LinearLayoutManager) chatMessageList.getLayoutManager();
        if (llm != null) {
            int numVisibleItems = llm.getChildCount();
            int numTotalItems = llm.getItemCount();
            int visibleItems = llm.findFirstVisibleItemPosition();
            return (visibleItems + numVisibleItems >= numTotalItems);
        }

        return false;
    }

    /**
     * Scroll the list of chat messages so last received message becomes visible
     */
    private void smoothScrollToLastChatMessage() {
        if (chatMessageListAdapter != null && chatMessageListAdapter.getItemCount() > 0) {
            chatMessageList.smoothScrollToPosition(chatMessageListAdapter.getItemCount() - 1);
        }
    }

    private void checkAndLoadChannelSettings() {
        currentCreatorSetting = null;
        Claim actualClaim = collectionClaimItem != null ? collectionClaimItem : fileClaim;
        if (actualClaim.getSigningChannel() != null) {
            loadChannelSettings(actualClaim.getSigningChannel().getClaimId());
        }
    }

    private void loadChannelSettings(final String channelId) {
        Activity activity = getActivity();
        if (activity != null) {
            ((OdyseeApp) activity.getApplication()).getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<String, Object> options = new HashMap<>();
                        options.put("channel_id", channelId);
                        options.put(Lbryio.AUTH_TOKEN_PARAM, Lbryio.AUTH_TOKEN);

                        okhttp3.Response response = Comments.performRequest(Lbry.buildJsonParams(options), "setting.Get");
                        ResponseBody responseBody = response.body();
                        JSONObject jsonResponse = new JSONObject(responseBody.string());
                        if (!jsonResponse.has("result") || jsonResponse.isNull("result")) {
                            throw new ApiCallException("invalid json response");
                        }

                        JSONObject result = Helper.getJSONObject("result", jsonResponse);
                        Type type = new TypeToken<CreatorSetting>(){}.getType();
                        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
                        CreatorSetting creatorSetting = gson.fromJson(result.toString(), type);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                didLoadChannelSettings(creatorSetting);
                            }
                        });
                    } catch (JSONException | ApiCallException | IOException ex) {
                        // pass
                    }
                }
            });
        }
    }

    private void didLoadChannelSettings(CreatorSetting creatorSetting) {
        currentCreatorSetting = creatorSetting;
    }

    class CodeAttributeProvider implements AttributeProvider {

        @Override
        public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
            Context context = getContext();
            if (node instanceof Code && context != null) {
                String colorCodeText = "#".concat(Integer.toHexString(ContextCompat.getColor(context, R.color.codeTagText) & 0x00ffffff));
                String colorCodeBg = "#".concat(Integer.toHexString(ContextCompat.getColor(context, R.color.codeTagBackground) & 0x00ffffff));
                String codeStyle = "display: inline-block; border-radius: 0.2rem" +
                        "; color: " + colorCodeText + "; background-color: " + colorCodeBg +
                        "; font-size: 0.8571rem; padding: calc(2rem/5 - 4px) calc(2rem/5) calc(2rem/5 - 5px);";
                attributes.put("style", codeStyle);
            }
        }
    }
}
