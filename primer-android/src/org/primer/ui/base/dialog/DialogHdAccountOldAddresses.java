/*
 *
 *  * Copyright 2014 http://Bither.net
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.primer.ui.base.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.primer.R;
import org.primer.primerj.api.http.PrimerUrl;
import org.primer.primerj.core.AbstractHD;
import org.primer.primerj.core.HDAccount;
import org.primer.primerj.utils.Utils;
import org.primer.ui.base.DropdownMessage;
import org.primer.util.StringUtil;
import org.primer.util.UIUtil;
import org.primer.util.WalletUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by songchenwen on 15/7/3.
 */
public class DialogHdAccountOldAddresses extends CenterDialog {
    private static final int MaxHeight = UIUtil.getScreenHeight() - UIUtil.dip2pix(200);
    private static final int ItemHeight = UIUtil.dip2pix(70);
    private static final int MAX_CACHE_SIZE = 500;

    private HDAccount hdAccount;

    private ListView lv;

    private Activity activity;

    private HashMap<Integer, String> addresses = new HashMap<Integer, String>();

    public DialogHdAccountOldAddresses(Activity context, HDAccount hdAccount) {
        super(context);
        this.hdAccount = hdAccount;
        activity = context;
        initView();
    }

    private void initView() {
        setContentView(R.layout.dialog_hd_old_addresses);
        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.getLayoutParams().height = caculateHeight();
    }

    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return issuedExternalAddressCount();
        }

        @Override
        public String getItem(int position) {
            String a = addresses.get(Integer.valueOf(position));
            if (a == null) {
                a = addressForIndex(position);
                if (addresses.size() >= MAX_CACHE_SIZE) {
                    addresses.clear();
                }
                addresses.put(Integer.valueOf(position), a);
            }
            return a;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item view;
            if (convertView == null || !(convertView instanceof Item)) {
                convertView = new Item(activity);
            }
            view = (Item) convertView;
            view.setAddress(getItem(position));
            return convertView;
        }
    };

    @Override
    public void show() {
        if (issuedExternalAddressCount() > 0) {
            super.show();
        }
    }

    private int caculateHeight() {
        int count = issuedExternalAddressCount();
        if (count <= 0) {
            return 0;
        }
        return Math.min(MaxHeight, count * ItemHeight);
    }

    private int issuedExternalAddressCount() {
            return hdAccount.issuedExternalIndex() + 1;
    }

    private String addressForIndex(int index) {
            return hdAccount.addressForPath(AbstractHD.PathType.EXTERNAL_ROOT_PATH, index)
                    .getAddress();
    }

    private void showMsg(int msg) {
        DropdownMessage.showDropdownMessage(activity, msg);
    }

    private class Item extends FrameLayout implements View.OnClickListener {
        private TextView tv;

        private String address;

        public Item(Context context) {
            super(context);
            removeAllViews();
            addView(LayoutInflater.from(context).inflate(R.layout
                    .list_item_hd_old_addresses_dialog, null), new LayoutParams(LayoutParams
                    .MATCH_PARENT, ItemHeight));
            findViewById(R.id.fl_address).setOnClickListener(this);
            tv = (TextView) findViewById(R.id.tv_subtransaction_address);
            findViewById(R.id.ibtn_view_on_net).setOnClickListener(this);
        }

        public void setAddress(String address) {
            this.address = address;
            tv.setText(WalletUtils.formatHash(address, 4, 12));
        }

        @Override
        public void onClick(View v) {
            dismiss();
            switch (v.getId()) {
                case R.id.fl_address:
                    StringUtil.copyString(address);
                    showMsg(R.string.copy_address_success);
                    break;
                case R.id.ibtn_view_on_net:
                    new DialogWithActions(activity) {
                        @Override
                        protected List<Action> getActions() {
                            ArrayList<Action> actions = new ArrayList<Action>();
                            actions.add(new Action(R.string
                                    .address_option_view_on_blockchain_info, new Runnable() {
                                @Override
                                public void run() {
                                    UIUtil.gotoBrower(activity, PrimerUrl
                                            .BLOCKCHAIN_INFO_ADDRESS_URL + address);
                                }
                            }));

                            String defaultCountry = Locale.getDefault().getCountry();
                            if (Utils.compareString(defaultCountry, "CN") || Utils.compareString
                                    (defaultCountry, "cn")) {
                                actions.add(new Action(R.string.address_option_view_on_btc,
                                        new Runnable() {
                                    @Override
                                    public void run() {
                                        UIUtil.gotoBrower(activity, PrimerUrl
                                                .BTC_COM_ADDRESS_URL + address);
                                    }
                                }));
                            }
                            return actions;
                        }
                    }.show();
                    break;
            }
        }
    }
}