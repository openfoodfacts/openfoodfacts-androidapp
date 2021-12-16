module Fastlane
  module Helper
    class SentryHelper
      def self.check_sentry_cli!
        unless `which sentry-cli`.include?('sentry-cli')
          UI.error("You have to install sentry-cli version #{Fastlane::Sentry::CLI_VERSION} to use this plugin")
          UI.error("")
          UI.error("Install it using:")
          UI.command("brew install getsentry/tools/sentry-cli")
          UI.error("OR")
          UI.command("curl -sL https://sentry.io/get-cli/ | bash")
          UI.error("If you don't have homebrew, visit http://brew.sh")
          UI.user_error!("Install sentry-cli and start your lane again!")
        end

        sentry_cli_version = Gem::Version.new(`sentry-cli --version`.scan(/(?:\d+\.?){3}/).first)
        required_version = Gem::Version.new(Fastlane::Sentry::CLI_VERSION)
        if sentry_cli_version < required_version
          UI.user_error!("Your sentry-cli is outdated, please upgrade to at least version #{Fastlane::Sentry::CLI_VERSION} and start your lane again!")
        end

        UI.success("sentry-cli #{sentry_cli_version} installed!")
      end

      def self.call_sentry_cli(command)
        UI.message "Starting sentry-cli..."
        require 'open3'
        error = []
        if FastlaneCore::Globals.verbose?
          UI.verbose("sentry-cli command:\n\n")
          UI.command(command.to_s)
          UI.verbose("\n\n")
        end
        final_command = command.map { |arg| Shellwords.escape(arg) }.join(" ")
        Open3.popen3(final_command) do |stdin, stdout, stderr, wait_thr|
          while (line = stdout.gets)
            UI.message(line.strip!)
          end
          while (line = stderr.gets)
            error << line.strip!
          end
          exit_status = wait_thr.value
          unless exit_status.success? && error.empty?
            handle_error(error)
          end
        end
      end

      def self.handle_error(errors)
        fatal = false
        for error in errors do
          if error
            if error =~ /error/
              UI.error(error.to_s)
              fatal = true
            else
              UI.verbose(error.to_s)
            end
          end
        end
        UI.user_error!('Error while calling Sentry CLI') if fatal
      end
    end
  end
end
