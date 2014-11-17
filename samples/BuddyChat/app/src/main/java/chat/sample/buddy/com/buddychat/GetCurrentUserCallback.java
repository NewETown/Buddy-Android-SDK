package chat.sample.buddy.com.buddychat;

import com.buddy.sdk.models.User;

/**
 * Created by shawn on 8/27/14.
 */
public abstract class GetCurrentUserCallback {

    public abstract void complete(User user);
}

