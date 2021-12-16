module Fastlane
  module Actions
    class SentryCreateReleaseAction < Action
      def self.run(params)
        require 'shellwords'

        Helper::SentryHelper.check_sentry_cli!
        Helper::SentryConfig.parse_api_params(params)

        version = params[:version]
        version = "#{params[:app_identifier]}@#{params[:version]}" if params[:app_identifier]
        version = "#{version}+#{params[:build]}" if params[:build]

        command = [
          "sentry-cli",
          "releases",
          "new",
          version
        ]
        command.push("--finalize") if params[:finalize].nil?

        Helper::SentryHelper.call_sentry_cli(command)
        UI.success("Successfully created release: #{version}")
      end

      #####################################################
      # @!group Documentation
      #####################################################

      def self.description
        "Create new releases for a project on Sentry"
      end

      def self.details
        [
          "This action allows you to create new releases for a project on Sentry.",
          "See https://docs.sentry.io/learn/cli/releases/#creating-releases for more information."
        ].join(" ")
      end

      def self.available_options
        Helper::SentryConfig.common_api_config_items + [
          FastlaneCore::ConfigItem.new(key: :version,
                                       description: "Release version to create on Sentry"),
          FastlaneCore::ConfigItem.new(key: :app_identifier,
                                      short_option: "-a",
                                      env_name: "SENTRY_APP_IDENTIFIER",
                                      description: "App Bundle Identifier, prepended to version",
                                      optional: true),
          FastlaneCore::ConfigItem.new(key: :build,
                                      short_option: "-b",
                                      description: "Release build to create on Sentry",
                                      optional: true),
          FastlaneCore::ConfigItem.new(key: :finalize,
                                       description: "Whether to finalize the release. If not provided or false, the release can be finalized using the finalize_release action",
                                       default_value: false,
                                       is_string: false,
                                       optional: true)
        ]
      end

      def self.return_value
        nil
      end

      def self.authors
        ["wschurman"]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
