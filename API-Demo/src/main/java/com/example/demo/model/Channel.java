package com.example.demo.model;

import java.util.Set;

public class Channel {


        private CharSequence uri;
        private Set<CharSequence> recipients;

        public Channel(CharSequence uri, Set<CharSequence> recipients) {
            this.uri = uri;
            this.recipients = recipients;
        }

        public CharSequence getUri () {
            return this.uri;
        }


        public Set<CharSequence> getRecipients() {
        return recipients;
        }
}
